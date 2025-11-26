package ilp.ilp_cw2.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ilp.ilp_cw2.raycasting.Raycasting;
import ilp.ilp_cw2.types.LngLat;

import java.util.*;

public class AStarLngLat {
    // List of available directions to travel in
    private static final double[] directionAngles = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private static final LngLat[] offsets = {
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[0]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[1]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[2]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[3]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[4]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[5]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[6]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[7]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[8]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[9]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[10]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[11]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[12]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[13]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[14]),
            Utils.getNextPosition(new LngLat(0, 0), directionAngles[15]),
    };

    protected static class Node {
        public final LngLat position;
        public final Node previous;
        public final double gCost;
        public final double totalCost;

        Node(LngLat position, Node previous, double gCost, double totalCost) {
            this.position = position;
            this.previous = previous;
            this.gCost = gCost;
            this.totalCost = totalCost;
        }
    }

    /**
     * Returns a list of adjacent positions
     * @param pos The starting position
     * @return A list of nodes that are adjacent
     */
    private static ArrayList<LngLat> getAdjacentPositions(LngLat pos) {
        ArrayList<LngLat> adjacentPositions = new ArrayList<>(16);

        for (LngLat offset : offsets) {
            adjacentPositions.add(pos.add(offset));
        }

        return adjacentPositions;
    }

    /**
     * Given a start point, an end point, and a list of restricted areas, returns a list of LngLat as the path, and a Double as the cost
     * @param from Start point
     * @param to End point
     * @param maxCost The maximum cost before giving up
     * @param regionLines The lines the path can't cross
     * @return A list of positions as the path
     */
    public static Pair<List<LngLat>, Double> AStarPathWithCost(LngLat from, LngLat to, double maxCost, List<Raycasting.Line> regionLines) {
        // The final node to build the path back from
        Node finalNode = null;

        PriorityQueue<Node> boundaryQueue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.totalCost));
        Map<LngLat, Node> boundaryMap = new HashMap<>();
        Set<LngLat> closedSet = new HashSet<>();

        // Initially, just add the starting node
        boundaryQueue.add(new Node(from, null, 0, Utils.getDistance(from, to)));
        boundaryMap.put(from, new Node(from, null, 0, Utils.getDistance(from, to)));

        // While there is at least one boundary node
        while (!boundaryQueue.isEmpty()) {
            // Select the current node as the lowest cost boundary node
            Node current = boundaryQueue.poll();

            // Skip stale nodes
            if (closedSet.contains(current.position)) continue;
            // If above max cost, just skip this node
            if (current.totalCost > maxCost) continue;

            // Remove current node from boundary nodes
            boundaryMap.remove(current.position);
            // Put the current node into the closed list
            closedSet.add(current.position);

            // If this node is close to the "to" position,
            // this is the final node. Set final node and break
            if (current.totalCost - current.gCost <= 0.00015) {
                finalNode = current;
                break;
            }

            // Otherwise, recalculate all costs for neighboring nodes
            // and if that node is already present, update the cost if it's
            // smaller
            List<LngLat> nextPositions = getAdjacentPositions(current.position);

            // For all next nodes
            for (LngLat nextPosition : nextPositions) {
                // If this node is already present in the closedNodes don't do anything
                if (closedSet.contains(nextPosition)) continue;

                // If this step intersects with any of the lines we can't cross, continue
                Raycasting.Line stepLine = new Raycasting.Line(current.position, nextPosition);
                boolean intersects = false;
                for (Raycasting.Line line : regionLines) {
                    if (Raycasting.intersects(stepLine, line)) {
                        intersects = true;
                        break;
                    }
                }
                if (intersects) continue;

                Node existingNode = boundaryMap.get(nextPosition);
                double newGCost = current.gCost + 0.00015;
                double newFCost = newGCost + Utils.getDistance(nextPosition, to) * 1.1;
                // If a node is already in the boundary
                if (existingNode == null) {
                    // Add new node to boundary
                    Node newNode = new Node(nextPosition, current, newGCost, newFCost);
                    boundaryMap.put(nextPosition, newNode);
                    boundaryQueue.add(newNode);
                } else {
                    // Check to see if this cost is lower
                    if (newFCost < existingNode.totalCost) {
                        // If this cost is lower, replace the node in the boundary with a new node
                        Node newNode = new Node(nextPosition, current, newGCost, newFCost);
                        boundaryMap.put(nextPosition, newNode);
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
            path.add(finalNode.position);
            finalNode = finalNode.previous;
        }

        return new Pair<>(path.reversed(), cost);
    }
}
