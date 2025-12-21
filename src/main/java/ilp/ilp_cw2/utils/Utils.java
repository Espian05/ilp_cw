package ilp.ilp_cw2.utils;

import ilp.ilp_cw2.dtos.*;
import ilp.ilp_cw2.raycasting.Raycasting;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.LngLatAlt;
import ilp.ilp_cw2.types.Region;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Utils {
    /**
     * Returns the euclidean distance between two positions
     * (because otherwise I'm redoing checks I *know* have to have happened in my implementation)
     * @param position1 The first position
     * @param position2 The second position
     * @return double distance between the points
     */
    public static double getDistance(LngLat position1, LngLat position2) {
        return Math.sqrt(Math.pow(position1.lng - position2.lng, 2) +
                Math.pow(position1.lat - position2.lat, 2));
    }

    /**
     * Returns whether the distance between two points is below a given threshold
     * (defined by the docs is 0.00015)
     * @param position1 The first position
     * @param position2 The second position
     * @return boolean
     */
    public static boolean isClose(LngLat position1, LngLat position2) {
        return getDistance(position1, position2) < 0.00015;
    }

    /**
     * Returns the next position from a given point at a given angle,
     * defined in the docs as a 0.00015 move in the given direction
     * (could be made more efficient using a lookup table, given
     *  the small amount of possible angles)
     * @param position The position to start from
     * @param angle The direction to move in
     * @return The next position
     */
    public static LngLat getNextPosition(LngLat position, double angle) {
        double radians = Math.toRadians(angle);
        LngLat offset = new LngLat(Math.cos(radians), Math.sin(radians));
        return position.add(offset.scale(0.00015));
    }

    /**
     * Returns whether this request's .position is inside this request's region
     * @return Whether the point is inside or outside
     */
    public static boolean isInRegion(Region region, LngLat position) {
        boolean inRegion = false;

        for (int i = 0; i < region.vertices.size() - 1; i++) {
            LngLat position1 = region.vertices.get(i);
            LngLat position2 = region.vertices.get(i + 1);

            // As defined in the doc, if the point is on an edge it is counted as "inside"
            if (checkIfOnLine(position1, position2, position)) return true;

            // If it intersects an even number it's outside, odd number then inside
            // achieved by flipping a boolean initially set to false
            if (intersects(position1, position2, position)) {
                inRegion = !inRegion;
            }
        }

        return inRegion;
    }

    /**
     * Checks if a point is on a line by first checking collinearity,
     * and then checking within the bounding box defined by the points
     * @param position1 The first corner
     * @param position2 The opposite corner
     * @param point The point to check
     * @return Whether the point is on the line
     */
    private static boolean checkIfOnLine(LngLat position1, LngLat position2, LngLat point) {
        if (getGradient(point, position1) == getGradient(point, position2)) {
            return point.lng <= Math.max(position1.lng, position2.lng) &&
                    point.lat <= Math.max(position1.lat, position2.lat) &&
                    point.lng >= Math.min(position1.lng, position2.lng) &&
                    point.lat >= Math.min(position1.lat, position2.lat);
        }
        return false;
    }

    /**
     * Checks whether a ray going from point to infinity off to the right
     * will intersect with the line from position1 to position2 using this
     * quite neat algorithm I found here:
     * https://rosettacode.org/wiki/Ray-casting_algorithm
     * Don't worry, I do actually understand it I didn't just copy it!
     * @param position1 The lower position
     * @param position2 The upper position
     * @param point The point to cast from
     * @return Whether the ray intersects the line
     */
    private static boolean intersects(LngLat position1, LngLat position2, LngLat point) {
        // Ensure that the first position is the lower of the two
        if (position1.lat > position2.lat) {
            return intersects(position2, position1, point);
        }

        // Essentially a bounds check, if the point is to the left, the ray must always collide
        // if it's above, below, or to the right, it can never collide
        if (point.lat > position2.lat) return false;
        if (point.lat < position1.lat) return false;
        if (point.lng > Math.max(position1.lng, position2.lng)) return false;
        if (point.lng < Math.min(position1.lng, position2.lng)) return true;

        // Otherwise, we check the gradients
        double mPosition2 = getGradient(position1, position2);
        double mPoint = getGradient(position1, point);

        return mPoint < mPosition2;
    }

    /**
     * Computes the gradient between two points
     * Specifically returns positive infinity if the lats are the same
     * because it works for my algorithm - this is not supposed to be general
     * @param position1 The first position
     * @param position2 The second position
     * @return The gradient
     */
    private static double getGradient(LngLat position1, LngLat position2) {
        if (position1.lng == position2.lng) return Double.POSITIVE_INFINITY;
        return (position1.lat - position2.lat) / (position1.lng - position2.lng);
    }

    public static boolean isInRegions(Region[] regions, LngLat position) {
        for (Region region : regions) {
            if (isInRegion(region, position)) return true;
        }
        return false;
    }

    public static List<Region> restrictedAreasToRegions(RestrictedArea[] restrictedAreas) {
        List<Region> regions = new ArrayList<>();

        for (RestrictedArea restrictedArea : restrictedAreas) {
            regions.add(new Region(restrictedArea.getName(),
                    new ArrayList<>(
                            restrictedArea.getVertices().stream().map(point -> new LngLat(point.lng, point.lat)).toList()
                    )
            ));
        }

        return regions;
    }

    public static boolean fpcEquality(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean isInRegionRaycasting(Region region, LngLat position) {
        // Convert region to a list of lines (assuming a valid region)
        List<Raycasting.Line> lines = new ArrayList<>();
        for (int i = 0; i < region.vertices.size() - 1; i++) {
            lines.add(new Raycasting.Line(region.vertices.get(i), region.vertices.get(i + 1)));
        }

        // Initialise a long line
        Raycasting.Line castLine = new Raycasting.Line(position, new LngLat(10000.0, 10000.0));

        // Initialise result boolean
        boolean inRegion = false;

        // Loop through the region lines, checking for an intersection
        // When one is found, flip the boolean
        for (Raycasting.Line line : lines) {
            if (Raycasting.findIntersectionPoint(line, castLine) != null) inRegion = !inRegion;
        }

        return inRegion;
    }

    public static boolean isInRegionsRaycasting(Region[] regions, LngLat position) {
        for (Region region : regions) {
            if (isInRegionRaycasting(region, position)) return true;
        }
        return false;
    }

    public static ArrayList<Raycasting.Line> regionsToLines(Region[] regions) {
        ArrayList<Raycasting.Line> lines = new ArrayList<>();
        for (Region region : regions) {
            for (int i = 0; i < region.vertices.size() - 1; i++) {
                Raycasting.Line line = new Raycasting.Line(region.vertices.get(i), region.vertices.get(i + 1));
                lines.add(line);
            }
        }
        return lines;
    }

    public static <T> @NotNull List<Pair<T, T>> generatePairs(List<T> original) {
        if (original.isEmpty()) return new ArrayList<>();

        List<Pair<T, T>> returnValue = new ArrayList<>();
        T first = original.removeFirst();
        for (T element : original) {
            returnValue.add(new Pair<>(first, element));
        }

        returnValue.addAll(generatePairs(original));
        return returnValue;
    }

    public static <T> ArrayList<ArrayList<T>> generatePermutations(ArrayList<T> list) {
        if (list.size() == 1) return new ArrayList<>(){{add(list);}};

        ArrayList<ArrayList<T>> returnValue = new ArrayList<>();

        for (T element : list) {
            ArrayList<T> copyList = new ArrayList<>(list);
            copyList.remove(element);
            returnValue.addAll(generatePermutations(copyList).stream().peek(perm -> perm.addFirst(element)).toList());
        }

        return returnValue;
    }

    /**
     * Given a set of things, returns a set of set of lists of all sublists
     * e.g.
     * {A, B, C} -> { {{ A, B, C }}, {{ A, B }, { C }}, {{ A }, { B, C }}, {{ A, C }, { B }}, {{ A }, { B }, { C }} }
     * @param set
     * @return
     * @param <T>
     */
    public static <T> HashSet<HashSet<HashSet<T>>> getSubsets(HashSet<T> set) {
        HashSet<HashSet<HashSet<T>>> sets = new HashSet<>();

        // Add the original set to your return set
        HashSet<HashSet<T>> originalSet = new HashSet<>();
        originalSet.add(set);
        sets.add(originalSet);

        if (set.size() == 1) return sets;

        for (T element : set) {
            HashSet<T> copySet = new HashSet<>(set);
            copySet.remove(element);

            HashSet<T> elementAsSet = new HashSet<>();
            elementAsSet.add(element);

            HashSet<HashSet<HashSet<T>>> subsets = getSubsets(copySet);
            for (HashSet<HashSet<T>> subset : subsets) {
                subset.add(elementAsSet);
                sets.add(subset);
            }
        }

        return sets;
    };

    private static final boolean queryAvailableDronesDebug = false;

    /**
     * Given some params, returns a list of drones that can complete this list of medDispatchRecords,
     * and for what estimated cost
     * @param medDispatchRecs
     * @param drones
     * @param dronesForServicePoints
     * @param servicePoints
     * @return
     */
    public static List<Pair<Drone, Double>> queryAvailableDrones(
            ArrayList<MedDispatchRec> medDispatchRecs,
            Drone[] drones,
            DronesForServicePoint[] dronesForServicePoints,
            ServicePoint[] servicePoints
    ) {
        return Arrays.stream(drones).filter(drone -> {
            List<Pair<DronesAvailability, Integer>> droneAvailabilityPairs = new ArrayList<>();
            for (DronesForServicePoint dronesForServicePoint : dronesForServicePoints) {
                for (DronesAvailability availability : dronesForServicePoint.getDrones()) {
                    if (availability.getId().equals(drone.id)) {
                        droneAvailabilityPairs.add(new Pair<>(availability, dronesForServicePoint.servicePointId));
                    }
                }
            }

            if (queryAvailableDronesDebug) {
                System.out.println();
                System.out.println("Considering drone " + drone.id);
            }

            // The id of the service point that this drone can do these deliveries from
            int servicePointId = 0;

            // Only want to return true if a drone matches at least one slot for each medDispatchRed
            // and fulfills all requirements
            // For every record
            // The service point the drone is at when it can fulfill all of the medDispatchRecs
            for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                boolean matchesThisRecord = false;

                // For each list of availability at each service point this drone services
                for (Pair<DronesAvailability, Integer> droneAvailabilityPair : droneAvailabilityPairs) {
                    // For each availability on a given day in a given schedule
                    for (Availability availability : droneAvailabilityPair.first.getAvailability()) {
                        // Check for day of week
                        if (!medDispatchRec.isDateNull())
                            if (!medDispatchRec.getDate().getDayOfWeek().equals(availability.getDayOfWeek())) continue;

                        // Check from and until times
                        if (!medDispatchRec.isTimeNull()) {
                            if (availability.getFrom().isAfter(medDispatchRec.getTime())) continue;
                            if (availability.getUntil().isBefore(medDispatchRec.getTime())) continue;
                        }

                        matchesThisRecord = true;
                        servicePointId = droneAvailabilityPair.second;
                        break;
                    }
                    if (matchesThisRecord) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Drone " + drone.id + " has matching availability with medDispatchRec " + medDispatchRec.getId());
                        break;
                    }
                }

                if (!matchesThisRecord) {
                    if (queryAvailableDronesDebug)
                        System.out.println("Drone " + drone.id + " does not have matching availability with with medDispatchRec " + medDispatchRec.getId());
                    return false;
                }

                // Now check through all requirements for this medDispatchRec
                Requirements requirements = medDispatchRec.getRequirements();
                if (requirements.getCooling() != null)
                    if (requirements.getCooling() && !drone.capability.isCooling()) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Drone " + drone.id + " does not match the cooling requirement with medDispatchRec " + medDispatchRec.getId());
                        return false;
                    }
                if (requirements.getHeating() != null)
                    if (requirements.getHeating() && !drone.capability.isHeating()) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Drone " + drone.id + " does not match the heating requirement with medDispatchRec " + medDispatchRec.getId());
                        return false;
                    }

                if (queryAvailableDronesDebug)
                    System.out.println("Drone " + drone.id + " has matching heating/cooling with medDispatchRec " + medDispatchRec.getId());
            }

            // Now that we know the individual requirements for each medDispatchRec are satisfied,
            // we can look at the total capacity
            double totalCapacityNeeded = 0;
            for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                totalCapacityNeeded += medDispatchRec.getRequirements().getCapacity();
            }
            if (totalCapacityNeeded > drone.capability.getCapacity()) {
                if (queryAvailableDronesDebug)
                    System.out.println("Drone " + drone.id + " does not have the capacity needed ( " + drone.capability.getCapacity() + " < " + totalCapacityNeeded + " )");
                return false;
            }

            for (ServicePoint servicePoint : servicePoints) {
                if (servicePoint.id == servicePointId) {
                    drone.servicePointPosition = new LngLat(servicePoint.location.lng, servicePoint.location.lat);
                    break;
                }
            }

            // Now we know it also has the capacity, so we can finally look at the maxCost
            // (distance(servicePoint, delivery)/step) × costPerMove + costInitial + costFinal
            ArrayList<LngLat> deliveryPoints = new ArrayList<>();
            for (MedDispatchRec rec : medDispatchRecs) {
                deliveryPoints.add(rec.getDelivery());
            }

            double lowestDistance = bestDeliverOrderWithCost(drone.servicePointPosition, deliveryPoints).second;

            boolean totalCostNeeded = false;
            for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                if (medDispatchRec.getRequirements().getMaxCost() != null) totalCostNeeded = true;
            }

            double totalEstimatedMoves = lowestDistance / 0.00015;
            double totalCost = totalEstimatedMoves;
            totalCost *= drone.capability.getCostPerMove();
            totalCost += drone.capability.getCostInitial();
            totalCost += drone.capability.getCostFinal();

            double proRataCost = totalCost / medDispatchRecs.size();
            drone.estimatedCost = proRataCost;

            if (totalEstimatedMoves > drone.capability.getMaxMoves()) {
                if (queryAvailableDronesDebug)
                    System.out.println("Estimated number of moves is too high (" + totalEstimatedMoves + " > " + drone.capability.getMaxMoves() + ")");
                return false;
            }

            if (totalCostNeeded) {
                if (queryAvailableDronesDebug)
                    System.out.println("Total Cost Needed. Considering all medDispatchRecs");
                for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                    if (medDispatchRec.getRequirements().maxCostIsNull()) {
                        System.out.println("medDispatchRec " + medDispatchRec.getId() + " max cost is null");
                        continue;
                    }
                    System.out.println("medDispatchRec " + medDispatchRec.getId() + " has max cost " + medDispatchRec.getRequirements().getMaxCost());
                    if (proRataCost > medDispatchRec.getRequirements().getMaxCost()) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Pro rata cost is too high for medDispatchRec " +
                                    medDispatchRec.getId() + " ( " + proRataCost + " > " +
                                    medDispatchRec.getRequirements().getMaxCost() + ")");
                        return false;
                    }
                    if (queryAvailableDronesDebug)
                        System.out.println("Pro rata cost lower than medDispatchRec " +
                                medDispatchRec.getId() + " ( " + proRataCost + " < " +
                                medDispatchRec.getRequirements().getMaxCost() + ")");
                }
            }

            if (queryAvailableDronesDebug)
                System.out.println("Drone " + drone.id + " matches with all medDispatchRecs");
            return true;
        }).map(drone -> new Pair<>(drone, drone.estimatedCost)).toList();
    }

    public static ArrayList<LngLat> pathFromPoints(ArrayList<LngLat> points, ArrayList<Raycasting.Line> lines) {
        ArrayList<LngLat> path = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            if (i == 0) path.addAll(AStarIntegerPosition.AStarPathWithCost(points.get(i), points.get(i + 1), 100, lines).first);
            else path.addAll(AStarIntegerPosition.AStarPathWithCost(path.getLast(), points.get(i + 1), 100, lines).first);
        }

        return path;
    }

    public static Pair<ArrayList<LngLat>, ArrayList<LngLat>> bestDeliveryOrderingPathAStar(LngLat servicePoint, ArrayList<LngLat> deliveryPoints, ArrayList<Raycasting.Line> lines) {
        // A valid delivery path must start and end at the service point

        // Generate all permutations of delivery points
        ArrayList<ArrayList<LngLat>> permutations = generatePermutations(deliveryPoints);

        ArrayList<LngLat> bestDeliveryOrder = null;
        ArrayList<LngLat> bestDeliveryPath = null;

        for (ArrayList<LngLat> ordering : permutations) {
            ordering.addFirst(servicePoint);
            ordering.addLast(servicePoint);

            ArrayList<LngLat> path = pathFromPoints(ordering, lines);

            if (bestDeliveryPath == null) {
                bestDeliveryPath = path;
                bestDeliveryOrder = ordering;
            } else if (path.size() < bestDeliveryPath.size()) {
                bestDeliveryPath = path;
                bestDeliveryOrder = ordering;
            }
        }

        return new Pair<>(bestDeliveryOrder, bestDeliveryPath);
    }

    public static Pair<ArrayList<LngLat>, Double> bestDeliverOrderWithCost(LngLat servicePoint, ArrayList<LngLat> deliveryPoints) {
        // Generate all permutations of delivery points
        ArrayList<ArrayList<LngLat>> permutations = generatePermutations(deliveryPoints);

        ArrayList<LngLat> bestDeliveryOrder = null;
        double shortestDistance = Double.MAX_VALUE;

        for (ArrayList<LngLat> ordering : permutations) {
            ordering.addFirst(servicePoint);
            ordering.addLast(servicePoint);

            double totalEstimatedDistance = 0;
            for (int i = 0; i < ordering.size() - 1; i++) {
                totalEstimatedDistance += Utils.getDistance(ordering.get(i), ordering.get(i + 1));
            }

            if (totalEstimatedDistance < shortestDistance) {
                shortestDistance = totalEstimatedDistance;
                bestDeliveryOrder = ordering;
            }
        }

        return new Pair<>(bestDeliveryOrder, shortestDistance);
    }

    public static Pair<ArrayList<LngLat>, ArrayList<LngLat>> bestDeliveryOrderingPathEuclidean(LngLat servicePoint, ArrayList<LngLat> deliveryPoints, ArrayList<Raycasting.Line> lines) {
        // A valid delivery path must start and end at the service point

        ArrayList<LngLat> bestDeliveryOrder = bestDeliverOrderWithCost(servicePoint, deliveryPoints).first;

        return new Pair<>(bestDeliveryOrder, pathFromPoints(bestDeliveryOrder, lines));
    }

    public static ArrayList<LngLat> bestDeliveryOrderingEuclidean(LngLat servicePoint, ArrayList<LngLat> deliveryPoints) {
        // A valid delivery path must start and end at the service point

        // Generate all permutations of delivery points
        ArrayList<ArrayList<LngLat>> permutations = generatePermutations(deliveryPoints);

        ArrayList<LngLat> bestDeliveryOrder = null;
        double shortestDistance = Double.MAX_VALUE;

        //System.out.println(permutations);
        //System.out.println(servicePoint);

        for (ArrayList<LngLat> ordering : permutations) {
            //System.out.println(ordering);
            ordering.addFirst(servicePoint);
            ordering.addLast(servicePoint);

            double totalEstimatedDistance = 0;
            for (int i = 0; i < ordering.size() - 1; i++) {
                totalEstimatedDistance += Utils.getDistance(ordering.get(i), ordering.get(i + 1));
            }

            if (totalEstimatedDistance < shortestDistance) {
                shortestDistance = totalEstimatedDistance;
                bestDeliveryOrder = ordering;
            }
        }

        return bestDeliveryOrder;
    }
}
