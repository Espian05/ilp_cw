package ilp.ilp_cw2.utils;

import ilp.ilp_cw2.dtos.RestrictedArea;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;

import java.util.*;
import java.util.stream.Collectors;

public class AStar {
    protected static class Node {
        public LngLat point;
        public Node previous;
        public double gCost;
        public double totalCost;

        Node(LngLat point, Node previous, double gCost, double totalCost) {
            this.point = point;
            this.previous = previous;
            this.gCost = gCost;
            this.totalCost = totalCost;
        }
    }

    // List of available directions to travel in
    private static final double[] directions = {
        0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private static final LngLat[] offsets = {
            Utils.getNextPosition(new LngLat(0, 0), directions[0]),
            Utils.getNextPosition(new LngLat(0, 0), directions[1]),
            Utils.getNextPosition(new LngLat(0, 0), directions[2]),
            Utils.getNextPosition(new LngLat(0, 0), directions[3]),
            Utils.getNextPosition(new LngLat(0, 0), directions[4]),
            Utils.getNextPosition(new LngLat(0, 0), directions[5]),
            Utils.getNextPosition(new LngLat(0, 0), directions[6]),
            Utils.getNextPosition(new LngLat(0, 0), directions[7]),
            Utils.getNextPosition(new LngLat(0, 0), directions[8]),
            Utils.getNextPosition(new LngLat(0, 0), directions[9]),
            Utils.getNextPosition(new LngLat(0, 0), directions[10]),
            Utils.getNextPosition(new LngLat(0, 0), directions[11]),
            Utils.getNextPosition(new LngLat(0, 0), directions[12]),
            Utils.getNextPosition(new LngLat(0, 0), directions[13]),
            Utils.getNextPosition(new LngLat(0, 0), directions[14]),
            Utils.getNextPosition(new LngLat(0, 0), directions[15]),
    };

    /**
     * Returns a list of adjacent positions
     * @param pos The starting position
     * @return A list of nodes that are adjacent
     */
    private static List<LngLat> getAdjacentPositions(LngLat pos) {
        return Arrays.stream(offsets).map(pos::add).collect(Collectors.toList());
    }

    /**
     * Given a start point, an end point, and a list of restricted areas, returns a list of LngLat as the path, and a Double as the cost
     * @param from Start point
     * @param to End point
     * @param maxCost The maximum cost before giving up
     * @param restrictedAreas The areas the path can't cross
     * @return A list of positions as the path
     */
    public static Pair<List<LngLat>, Double> AStarPathWithCost(LngLat from, LngLat to, double maxCost, RestrictedArea[] restrictedAreas) {
        // The final node to build the path back from
        Node finalNode = null;

        PriorityQueue<Node> boundaryQueue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.totalCost));
        Map<LngLat, Node> boundaryMap = new HashMap<>();
        Set<LngLat> closedSet = new HashSet<>();

        // Initially, just add the starting node
        boundaryQueue.add(new Node(from, null, 0, Utils.getDistance(from, to)));
        boundaryMap.put(from.getRounded(), new Node(from, null, 0, Utils.getDistance(from, to)));

        // While there is at least one boundary node
        while (!boundaryQueue.isEmpty()) {
            // Select the current node as the lowest cost boundary node
            Node current = boundaryQueue.poll();
            LngLat currentRounded = current.point.getRounded();

            // Skip stale nodes
            if (closedSet.contains(current.point)) continue;

            // If above max cost, just skip this node
            if (current.totalCost > maxCost) continue;

            // Remove current node from boundary nodes
            boundaryMap.remove(currentRounded);
            // Put the current node into the closed list
            closedSet.add(currentRounded);

            // If this node is close to the "to" position,
            // this is the final node. Set final node and break
            if (Utils.isClose(current.point, to)) {
                finalNode = current;
                break;
            }

            // Otherwise, recalculate all costs for neighboring nodes
            // and if that node is already present, update the cost if it's
            // smaller
            List<LngLat> nextPositions = getAdjacentPositions(current.point);

            // For all next nodes
            for (LngLat nextPosition : nextPositions) {
                LngLat nextPositionRounded = nextPosition.getRounded();
                // If this node is already present in the closedNodes don't do anything
                if (closedSet.contains(nextPositionRounded)) continue;

                // If the next position is in a restricted area, continue
                boolean inAnyRegions = false;
                for (RestrictedArea restrictedArea : restrictedAreas) {
                    Region newRegion = new Region(restrictedArea.getName(),
                            new ArrayList<>(
                                    restrictedArea.getVertices().stream().map(point -> new LngLat(point.lng, point.lat)).toList()
                            )
                    );
                    if (Utils.isInRegion(newRegion, nextPosition)) {
                        System.out.println("Is in region: " + newRegion.getName());
                        inAnyRegions = true;
                        break;
                    }
                }
                if (inAnyRegions) {
                    closedSet.add(nextPositionRounded);
                    continue;
                }

                Node existingNode = boundaryMap.get(nextPositionRounded);
                double newGCost = current.gCost + 0.00015;
                double newFCost = newGCost + Utils.getDistance(nextPosition, to);
                // If a node is already in the boundary
                if (existingNode == null) {
                    // Add new node to boundary
                    Node newNode = new Node(nextPosition, current, newGCost, newFCost);
                    boundaryMap.put(nextPositionRounded, newNode);
                    boundaryQueue.add(newNode);
                } else {
                    // Check to see if this cost is lower
                    if (newFCost < existingNode.totalCost) {
                        // If this cost is lower, replace the node in the boundary with a new node
                        Node newNode = new Node(nextPosition, current, newGCost, newFCost);
                        boundaryMap.put(nextPositionRounded, newNode);
                        boundaryQueue.add(newNode);
                    }
                }
            }
        }

        // If the final node is null, return an empty list
        if (finalNode == null) return new Pair<>(new ArrayList<>(), -1.0);

        // Otherwise, trace back from the final node and construct a path
        List<LngLat> path = new ArrayList<>();
        double cost = finalNode.gCost;
        while (finalNode != null) {
            path.add(finalNode.point);
            finalNode = finalNode.previous;
        }

        return new Pair<>(path.reversed(), cost);
    }
}
