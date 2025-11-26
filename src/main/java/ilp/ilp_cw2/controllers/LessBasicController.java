package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.dtos.*;
import ilp.ilp_cw2.raycasting.Raycasting;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;
import ilp.ilp_cw2.utils.AStarIntegerPosition;
import ilp.ilp_cw2.utils.GeoJson;
import ilp.ilp_cw2.utils.Pair;
import ilp.ilp_cw2.utils.Utils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class LessBasicController {
    private final String endpointStart = "/api/v1";

    private final RestTemplate restTemplate = new RestTemplate();

    private final String ilpEndpoint;

    @Autowired
    LessBasicController(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
    }

    // Debug booleans
    boolean queryAvailableDronesDebug = true;

    @GetMapping(endpointStart + "/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable boolean state) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        if (drones == null) return ResponseEntity.ok(new ArrayList<>());

        return ResponseEntity.ok(
                Arrays.stream(drones).filter(
                drone -> drone.capability != null && drone.capability.isCooling() == state
        ).map(Drone::getId).toList());
    }

    @GetMapping(endpointStart + "/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable String id) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        if (drones == null) return ResponseEntity.ok(null);

        // Locate the drone with that id
        List<Drone> dronesWithId = Arrays.stream(drones).filter(drone -> drone.id.equals(id)).toList();

        // If no drones with the given id are found, return a 404 "not found"
        // (the only time this is done in this coursework)
        if (dronesWithId.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(dronesWithId.getFirst());
    }

    @GetMapping(endpointStart + "/queryAsPath/{attribute}/{value}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attribute, @PathVariable String value) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        if (drones == null) return ResponseEntity.ok(new ArrayList<>());

        return ResponseEntity.ok(Arrays.stream(drones).filter(
        drone -> switch (attribute) {
            case "cooling" -> drone.capability.isCooling() == Boolean.parseBoolean(value);
            case "heating" -> drone.capability.isHeating() == Boolean.parseBoolean(value);
            case "capacity" -> drone.capability.getCapacity() == Double.parseDouble(value);
            case "maxMoves" -> drone.capability.getMaxMoves() == Integer.parseInt(value);
            case "costPerMove" -> drone.capability.getCostPerMove() == Double.parseDouble(value);
            case "costInitial" -> drone.capability.getCostInitial() == Double.parseDouble(value);
            case "costFinal" -> drone.capability.getCostFinal() == Double.parseDouble(value);
            default -> false;
        }).map(drone -> drone.id).toList());
    }

    @PostMapping(endpointStart + "/query")
    public ResponseEntity<List<String>> query(@RequestBody AttributeQuery[] attributeQueries) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);

        return ResponseEntity.ok(Arrays.stream(drones).filter(drone -> {
            for (AttributeQuery attributeQuery : attributeQueries) {
                boolean matchesQuery = switch (attributeQuery.getOperator()) {
                    case "=" -> switch (attributeQuery.getAttribute()) {
                        case "cooling" -> drone.capability.isCooling() == Boolean.parseBoolean(attributeQuery.getValue());
                        case "heating" -> drone.capability.isHeating() == Boolean.parseBoolean(attributeQuery.getValue());
                        case "capacity" -> drone.capability.getCapacity() == Double.parseDouble(attributeQuery.getValue());
                        case "maxMoves" -> drone.capability.getMaxMoves() == Integer.parseInt(attributeQuery.getValue());
                        case "costPerMove" -> drone.capability.getCostPerMove() == Double.parseDouble(attributeQuery.getValue());
                        case "costInitial" -> drone.capability.getCostInitial() == Double.parseDouble(attributeQuery.getValue());
                        case "costFinal" -> drone.capability.getCostFinal() == Double.parseDouble(attributeQuery.getValue());
                        default -> false;
                    };
                    case "!=" -> switch (attributeQuery.getAttribute()) {
                        case "cooling" -> drone.capability.isCooling() != Boolean.parseBoolean(attributeQuery.getValue());
                        case "heating" -> drone.capability.isHeating() != Boolean.parseBoolean(attributeQuery.getValue());
                        case "capacity" -> drone.capability.getCapacity() != Double.parseDouble(attributeQuery.getValue());
                        case "maxMoves" -> drone.capability.getMaxMoves() != Integer.parseInt(attributeQuery.getValue());
                        case "costPerMove" -> drone.capability.getCostPerMove() != Double.parseDouble(attributeQuery.getValue());
                        case "costInitial" -> drone.capability.getCostInitial() != Double.parseDouble(attributeQuery.getValue());
                        case "costFinal" -> drone.capability.getCostFinal() != Double.parseDouble(attributeQuery.getValue());
                        default -> false;
                    };
                    case ">" -> switch (attributeQuery.getAttribute()) {
                        case "capacity" -> drone.capability.getCapacity() > Double.parseDouble(attributeQuery.getValue());
                        case "maxMoves" -> drone.capability.getMaxMoves() > Integer.parseInt(attributeQuery.getValue());
                        case "costPerMove" -> drone.capability.getCostPerMove() > Double.parseDouble(attributeQuery.getValue());
                        case "costInitial" -> drone.capability.getCostInitial() > Double.parseDouble(attributeQuery.getValue());
                        case "costFinal" -> drone.capability.getCostFinal() > Double.parseDouble(attributeQuery.getValue());
                        default -> false;
                    };
                    case "<" -> switch (attributeQuery.getAttribute()) {
                        case "capacity" -> drone.capability.getCapacity() < Double.parseDouble(attributeQuery.getValue());
                        case "maxMoves" -> drone.capability.getMaxMoves() < Integer.parseInt(attributeQuery.getValue());
                        case "costPerMove" -> drone.capability.getCostPerMove() < Double.parseDouble(attributeQuery.getValue());
                        case "costInitial" -> drone.capability.getCostInitial() < Double.parseDouble(attributeQuery.getValue());
                        case "costFinal" -> drone.capability.getCostFinal() < Double.parseDouble(attributeQuery.getValue());
                        default -> false;
                    };
                    default -> false;
                };
                if (!matchesQuery) return false;
            }
            return true;
        }).map(drone -> { return drone.id; }).toList());
    }

    @PostMapping(endpointStart + "/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(@RequestBody ArrayList<MedDispatchRec> medDispatchRecs) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);

        List<Pair<Drone, Double>> availableDrones = Utils.queryAvailableDrones(medDispatchRecs, drones, dronesForServicePoints, servicePoints);
        return ResponseEntity.ok(availableDrones.stream().map(pair -> pair.first.id).toList());
    }

    @PostMapping(endpointStart + "/calcDeliveryPath")
    public ResponseEntity<DeliveryPaths> calcDeliveryPath(@RequestBody ArrayList<MedDispatchRec> medDispatchRecs) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        // Get all sublists of the medDispatchRecs

        // Create a set of all the medDispatchRecs
        HashSet<MedDispatchRec> medDispatchRecSet = new HashSet<>(medDispatchRecs);

        // Get all subsets of medDispatchRecs
        HashSet<HashSet<HashSet<MedDispatchRec>>> medDispatchRecSubsets = Utils.getSubsets(medDispatchRecSet);

        // Turn the subsets into sublists
        ArrayList<ArrayList<ArrayList<MedDispatchRec>>> medDispatchRecSubsetLists = new ArrayList<>();
        for (HashSet<HashSet<MedDispatchRec>> hashSetHashSet : medDispatchRecSubsets) {
            ArrayList<ArrayList<MedDispatchRec>> subsetLists = new ArrayList<>();
            for (HashSet<MedDispatchRec> hashSet : hashSetHashSet) {
                ArrayList<MedDispatchRec> subsetList = new ArrayList<>();
                for (MedDispatchRec medDispatchRec : hashSet) {
                    subsetList.add(medDispatchRec);
                }
                subsetLists.add(subsetList);
            }
            medDispatchRecSubsetLists.add(subsetLists);
        }

        // For each sublist, run queryAvailableDrones
        ArrayList<ArrayList<Pair<Drone, Double>>> deliveryProposalSublists = new ArrayList<>();
        for (ArrayList<ArrayList<MedDispatchRec>> sublistList : medDispatchRecSubsetLists) {
            // This is a list of groupings of medDispatchRec
            // Goal is to pair a drone with each of these groupings
            ArrayList<Pair<Drone, Double>> deliveryProposalPairList = new ArrayList<>();
            for (ArrayList<MedDispatchRec> subsetList : sublistList) {
                List<Pair<Drone, Double>> deliveryProposalPairs = Utils.queryAvailableDrones(subsetList, drones, dronesForServicePoints, servicePoints);

                // If there are no drones that can complete this dispatch, add a null pair
                if (deliveryProposalPairs.isEmpty()) {
                    deliveryProposalPairList.add(null);
                    continue;
                }

                // Find the lowest cost pair
                Pair<Drone, Double> bestPair = deliveryProposalPairs.getFirst();
                for (Pair<Drone, Double> pair : deliveryProposalPairs.subList(1, deliveryProposalPairs.size())) {
                    if (pair.second < bestPair.second) bestPair = pair;
                }

                // Add the lowest cost pair our list
                deliveryProposalPairList.add(bestPair);
            }
            deliveryProposalSublists.add(deliveryProposalPairList);
        }
        // I end up with a list of pairs of a sublist of medDispatchRec and the drones that can do it

        // Total the cost for all subgroups that are able to be completed
        ArrayList<Double> deliveryCosts = new ArrayList<>();
        for (ArrayList<Pair<Drone, Double>> deliveryProposalPairList : deliveryProposalSublists) {
            Double deliveryCost = 0.0;
            for (Pair<Drone, Double> pair : deliveryProposalPairList) {
                // If we find a null instead of a pair, this sublist was not completable
                if (pair == null) {
                    deliveryCost = null;
                    break;
                }
                // Otherwise, add the cost
                deliveryCost += pair.second;
            }
            deliveryCosts.add(deliveryCost);
        }

        // Find the group with the lowest cost
        ArrayList<Pair<Drone, Double>> bestDeliveryProposal = null;
        ArrayList<ArrayList<MedDispatchRec>> bestDeliveryRecSubsets = null;
        double lowestCostPlan = Double.MAX_VALUE;
        for (int i = 0; i < deliveryCosts.size(); i++) {
            if (deliveryCosts.get(i) == null) continue;
            if (deliveryCosts.get(i) >= lowestCostPlan) continue;
            lowestCostPlan = deliveryCosts.get(i);
            bestDeliveryProposal = deliveryProposalSublists.get(i);
            bestDeliveryRecSubsets = medDispatchRecSubsetLists.get(i);
        }

        // If there is no valid plan return empty object
        if (bestDeliveryProposal == null) {
            return ResponseEntity.ok(new DeliveryPaths());
        }

        // Get the lines from the restricted areas
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        // Generate list of paths for each grouping
        ArrayList<DronePath> dronePaths = new ArrayList<>();
        int totalMoves = 0;
        double totalCost = 0;
        for (int i = 0; i < bestDeliveryRecSubsets.size(); i++) {
            // Get list of delivery points from the medDispatchRecs
            ArrayList<LngLat> deliveryPoints = new ArrayList<>();
            for (MedDispatchRec rec : bestDeliveryRecSubsets.get(i)) {
                deliveryPoints.add(rec.getDelivery());
            }

            Drone drone = bestDeliveryProposal.get(i).first;

            // Get the best ordering of the points
            ArrayList<LngLat> bestOrdering = Utils.bestDeliveryOrderingEuclidean(drone.servicePointPosition, deliveryPoints);

            // Calculate paths
            ArrayList<ArrayList<LngLat>> paths = new ArrayList<>();
            int thisDroneMoves = 0;
            for (int x = 0; x < bestOrdering.size() - 1; x++) {
                ArrayList<LngLat> path;
                if (x == 0) path = AStarIntegerPosition.AStarPathWithCost(bestOrdering.get(x), bestOrdering.get(x + 1), 100, lines).first;
                else path = AStarIntegerPosition.AStarPathWithCost(paths.getLast().getLast(), bestOrdering.get(x + 1), 100, lines).first;
                path.add(path.getLast());
                thisDroneMoves += path.size() - 1;
                paths.add(path);
            }

            // Convert paths to a drone path
            DronePath dronePath = new DronePath();
            dronePath.droneId = drone.id;

            List<Delivery> deliveries = new ArrayList<>();
            for (int index = 0; index < paths.size(); index++) {
                Delivery delivery = new Delivery();
                if (index < bestDeliveryRecSubsets.get(i).size()) delivery.deliveryId = bestDeliveryRecSubsets.get(i).get(index).getId();
                else delivery.deliveryId = null;
                delivery.flightPath = paths.get(index);
                deliveries.add(delivery);
            }
            dronePath.deliveries = deliveries.toArray(new Delivery[0]);

            totalCost += thisDroneMoves * drone.capability.getCostPerMove() + drone.capability.getCostInitial() + drone.capability.getCostFinal();
            totalMoves += thisDroneMoves;

            dronePaths.add(dronePath);
        }

        // Convert paths to delivery paths
        DeliveryPaths deliveryPaths = new DeliveryPaths();

        deliveryPaths.totalCost = totalCost;
        deliveryPaths.totalMoves = totalMoves;

        deliveryPaths.dronePaths = dronePaths;

        return ResponseEntity.ok(deliveryPaths);
    }

    @PostMapping(endpointStart + "/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody ArrayList<MedDispatchRec> medDispatchRecs) throws JSONException {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        // Get all sublists of the medDispatchRecs

        // Create a set of all the medDispatchRecs
        HashSet<MedDispatchRec> medDispatchRecSet = new HashSet<>(medDispatchRecs);

        // Get all subsets of medDispatchRecs
        HashSet<HashSet<HashSet<MedDispatchRec>>> medDispatchRecSubsets = Utils.getSubsets(medDispatchRecSet);

        // Turn the subsets into sublists
        ArrayList<ArrayList<ArrayList<MedDispatchRec>>> medDispatchRecSubsetLists = new ArrayList<>();
        for (HashSet<HashSet<MedDispatchRec>> hashSetHashSet : medDispatchRecSubsets) {
            ArrayList<ArrayList<MedDispatchRec>> subsetLists = new ArrayList<>();
            for (HashSet<MedDispatchRec> hashSet : hashSetHashSet) {
                ArrayList<MedDispatchRec> subsetList = new ArrayList<>();
                for (MedDispatchRec medDispatchRec : hashSet) {
                    subsetList.add(medDispatchRec);
                }
                subsetLists.add(subsetList);
            }
            medDispatchRecSubsetLists.add(subsetLists);
        }

        // For each sublist, run queryAvailableDrones
        ArrayList<ArrayList<Pair<Drone, Double>>> deliveryProposalSublists = new ArrayList<>();
        for (ArrayList<ArrayList<MedDispatchRec>> sublistList : medDispatchRecSubsetLists) {
            // This is a list of groupings of medDispatchRec
            // Goal is to pair a drone with each of these groupings
            ArrayList<Pair<Drone, Double>> deliveryProposalPairList = new ArrayList<>();
            for (ArrayList<MedDispatchRec> subsetList : sublistList) {
                List<Pair<Drone, Double>> deliveryProposalPairs = Utils.queryAvailableDrones(subsetList, drones, dronesForServicePoints, servicePoints);

                // If there are no drones that can complete this dispatch, add a null pair
                if (deliveryProposalPairs.isEmpty()) {
                    deliveryProposalPairList.add(null);
                    continue;
                }

                // Find the lowest cost pair
                Pair<Drone, Double> bestPair = deliveryProposalPairs.getFirst();
                for (Pair<Drone, Double> pair : deliveryProposalPairs.subList(1, deliveryProposalPairs.size())) {
                    if (pair.second < bestPair.second) bestPair = pair;
                }

                // Add the lowest cost pair our list
                deliveryProposalPairList.add(bestPair);
            }
            deliveryProposalSublists.add(deliveryProposalPairList);
        }
        // I end up with a list of pairs of a sublist of medDispatchRec and the drones that can do it

        // Total the cost for all subgroups that are able to be completed
        ArrayList<Double> deliveryCosts = new ArrayList<>();
        for (ArrayList<Pair<Drone, Double>> deliveryProposalPairList : deliveryProposalSublists) {
            Double deliveryCost = 0.0;
            for (Pair<Drone, Double> pair : deliveryProposalPairList) {
                // If we find a null instead of a pair, this sublist was not completable
                if (pair == null) {
                    deliveryCost = null;
                    break;
                }
                // Otherwise, add the cost
                deliveryCost += pair.second;
            }
            deliveryCosts.add(deliveryCost);
        }

        // Find the group with the lowest cost
        ArrayList<Pair<Drone, Double>> bestDeliveryProposal = null;
        ArrayList<ArrayList<MedDispatchRec>> bestDeliveryRecSubsets = null;
        double lowestCostPlan = Double.MAX_VALUE;
        for (int i = 0; i < deliveryCosts.size(); i++) {
            if (deliveryCosts.get(i) == null) continue;
            if (deliveryCosts.get(i) >= lowestCostPlan) continue;
            lowestCostPlan = deliveryCosts.get(i);
            bestDeliveryProposal = deliveryProposalSublists.get(i);
            bestDeliveryRecSubsets = medDispatchRecSubsetLists.get(i);
        }

        // If there is no valid plan return empty object
        if (bestDeliveryProposal == null) {
            return ResponseEntity.ok("");
        }

        // Get the lines from the restricted areas
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        // Generate list of paths for each grouping
        ArrayList<DronePath> dronePaths = new ArrayList<>();
        int totalMoves = 0;
        double totalCost = 0;
        for (int i = 0; i < bestDeliveryRecSubsets.size(); i++) {
            // Get list of delivery points from the medDispatchRecs
            ArrayList<LngLat> deliveryPoints = new ArrayList<>();
            for (MedDispatchRec rec : bestDeliveryRecSubsets.get(i)) {
                deliveryPoints.add(rec.getDelivery());
            }

            Drone drone = bestDeliveryProposal.get(i).first;

            // Get the best ordering of the points
            ArrayList<LngLat> bestOrdering = Utils.bestDeliveryOrderingEuclidean(drone.servicePointPosition, deliveryPoints);

            // Calculate paths
            ArrayList<ArrayList<LngLat>> paths = new ArrayList<>();
            int thisDroneMoves = 0;
            for (int x = 0; x < bestOrdering.size() - 1; x++) {
                ArrayList<LngLat> path;
                if (x == 0) path = AStarIntegerPosition.AStarPathWithCost(bestOrdering.get(x), bestOrdering.get(x + 1), 100, lines).first;
                else path = AStarIntegerPosition.AStarPathWithCost(paths.getLast().getLast(), bestOrdering.get(x + 1), 100, lines).first;
                path.add(path.getLast());
                thisDroneMoves += path.size() - 1;
                paths.add(path);
            }

            // Convert paths to a drone path
            DronePath dronePath = new DronePath();
            dronePath.droneId = drone.id;

            List<Delivery> deliveries = new ArrayList<>();
            for (int index = 0; index < paths.size(); index++) {
                Delivery delivery = new Delivery();
                if (index < bestDeliveryRecSubsets.get(i).size()) delivery.deliveryId = bestDeliveryRecSubsets.get(i).get(index).getId();
                else delivery.deliveryId = null;
                delivery.flightPath = paths.get(index);
                deliveries.add(delivery);
            }
            dronePath.deliveries = deliveries.toArray(new Delivery[0]);

            totalCost += thisDroneMoves * drone.capability.getCostPerMove() + drone.capability.getCostInitial() + drone.capability.getCostFinal();
            totalMoves += thisDroneMoves;

            dronePaths.add(dronePath);
        }

        ArrayList<ArrayList<LngLat>> paths = new ArrayList<>();
        for (DronePath dronePath : dronePaths) {
            for (Delivery delivery : dronePath.deliveries) {
                paths.add(delivery.flightPath);
            }
        }

        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(paths, restrictedAreas));
    }

    @GetMapping("/A_Star_Test")
    public ResponseEntity<String> aStarTest() throws JSONException {
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        LngLat appleton = new LngLat(-3.1863580788986368, 55.94468066708487);
        LngLat oceanTerminal = new LngLat(-3.17732611501824, 55.981186279333656);

        LngLat hamilton = new LngLat(-4.035783909780434, 55.7648450808785);
        LngLat brightonStreet = new LngLat(-3.1893772182582154, 55.946378903626425);

        LngLat annoyingPlace = new LngLat(-3.1875123178551235, 55.945254809917685);
        LngLat annoyingPlace2 = new LngLat(-3.1893837696125047, 55.94540438103067);

        LngLat testPoint1 = new LngLat(-3.190552655673514, 55.943019164584655);
        LngLat testPoint2 = new LngLat(-3.189387176308202, 55.94576879534256);

        LngLat hafsas = new LngLat(-3.1851137499146205, 55.944578297383686);

        ArrayList<LngLat> deliveryPoints = new ArrayList<>();
        deliveryPoints.add(brightonStreet);
        deliveryPoints.add(annoyingPlace);
        deliveryPoints.add(annoyingPlace2);
        deliveryPoints.add(testPoint1);
        deliveryPoints.add(testPoint2);
        deliveryPoints.add(hafsas);
        deliveryPoints.add(oceanTerminal);
        deliveryPoints.add(hamilton);

        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        ArrayList<Raycasting.Line> lines = Utils.regionsToLines(regions);


        ArrayList<LngLat> bestPath = Utils.bestDeliveryOrderingPathEuclidean(appleton, deliveryPoints, lines).second;
        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(new ArrayList<>(){{add(bestPath);}}, restrictedAreas));
    }

    @GetMapping("/sublist_test")
    public static ResponseEntity<String> sublistTest() {
        ArrayList<String> test_list = new ArrayList<>();
        test_list.add("A");
        test_list.add("B");
        test_list.add("C");

        HashSet<String> set = new HashSet<>(test_list);

        return ResponseEntity.ok(Utils.getSubsets(set).toString());
    }
}
