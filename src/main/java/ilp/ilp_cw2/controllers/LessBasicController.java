package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.dtos.*;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.LngLatAlt;
import ilp.ilp_cw2.utils.AStar;
import ilp.ilp_cw2.utils.GeoJson;
import ilp.ilp_cw2.utils.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        return ResponseEntity.ok(Arrays.stream(drones).filter(drone -> {
                return drone.capability != null && drone.capability.isCooling() == state;
            }
        ).map(drone -> {
            return drone.getId();
        }).toList());
    }

    @GetMapping(endpointStart + "/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable String id) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);

        return ResponseEntity.ok(Arrays.stream(drones).filter(drone -> { return drone.id.equals(id); }).toList().getFirst());
    }

    @GetMapping(endpointStart + "/queryAsPath/{attribute}/{value}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attribute, @PathVariable String value) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);

        return ResponseEntity.ok(Arrays.stream(drones).filter(drone -> {
            return switch (attribute) {
                case "cooling" -> drone.capability.isCooling() == Boolean.parseBoolean(value);
                case "heating" -> drone.capability.isHeating() == Boolean.parseBoolean(value);
                case "capacity" -> drone.capability.getCapacity() == Double.parseDouble(value);
                case "maxMoves" -> drone.capability.getMaxMoves() == Integer.parseInt(value);
                case "costPerMove" -> drone.capability.getCostPerMove() == Double.parseDouble(value);
                case "costInitial" -> drone.capability.getCostInitial() == Double.parseDouble(value);
                case "costFinal" -> drone.capability.getCostFinal() == Double.parseDouble(value);
                default -> false;
            };
        }).map(drone -> { return drone.id; }).toList());
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
    public ResponseEntity<List<String>> queryAvailableDrones(@RequestBody MedDispatchRec[] medDispatchRecs) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);

        return ResponseEntity.ok(Arrays.stream(drones).filter(drone -> {
            List<DronesAvailability> droneAvailabilities = new ArrayList<>();
            for (DronesForServicePoint dronesForServicePoint : dronesForServicePoints) {
                for (DronesAvailability availability : dronesForServicePoint.getDrones()) {
                    if (availability.getId().equals(drone.id)) {
                        droneAvailabilities.add(availability);
                    }
                }
            }

            // Only want to return true if a drone matches at least one slot for each medDispatchRed
            // and fulfills all requirements
            // For every record
            // The service point the drone is at when it can fulfill all of the medDispatchRecs
            LngLat servicePointLocation;
            for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                boolean matchesThisRecord = false;

                // For each list of availability at each service point this drone services
                for (DronesAvailability droneAvailability : droneAvailabilities) {
                    // For each availability on a given day in a given schedule
                    for (Availability availability : droneAvailability.getAvailability()) {
                        if (!medDispatchRec.getDate().getDayOfWeek().equals(availability.getDayOfWeek())) continue;
                        if (availability.getFrom().isAfter(medDispatchRec.getTime())) continue;
                        if (availability.getUntil().isBefore(medDispatchRec.getTime())) continue;
                        matchesThisRecord = true;
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
                    if (requirements.getCooling() != drone.capability.isCooling()) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Drone " + drone.id + " does not match the cooling requirement with medDispatchRec " + medDispatchRec.getId());
                        return false;
                    }
                if (requirements.getHeating() != null)
                    if (requirements.getHeating() != drone.capability.isHeating()) {
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

            // ArrayList<Drone> requirementDrones = new ArrayList<>();
            // ArrayList<String> timeDrones = new ArrayList<>();
            // requirementDrones.stream().filter(drone -> { return timeDrones.contains(drone.id); });

            // Now we know it also has the capacity, so we can finally look at the maxCost
            // (distance(servicePoint, delivery)/step) × costPerMove + costInitial + costFinal
            double totalCost = 0;
            boolean totalCostNeeded = false;
            for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                if (medDispatchRec.getRequirements().getMaxCost() != null) totalCostNeeded = true;
                //totalCost += distance
            }

            if (totalCostNeeded) {
                for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                    if (totalCost > medDispatchRec.getRequirements().getMaxCost()) {
                        if (queryAvailableDronesDebug)
                            System.out.println("Max cost is too high for medDispatchRec " +
                                    medDispatchRec.getId() + " ( " + totalCost + " > " +
                                    medDispatchRec.getRequirements().getMaxCost() + ")");
                        return false;
                    }
                }
            }

            if (queryAvailableDronesDebug)
                System.out.println("Drone " + drone.id + " matches with all medDispatchRecs");
            return true;
        }).map(drone -> { return drone.id; }).toList());
    }

    @GetMapping("/service-points-geo")
    public ResponseEntity<String> getServicePoints() throws JSONException {
        ServicePoint[] service_points =
                restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);

        JSONObject response = new JSONObject();
        response.put("type", "FeatureCollection");

        JSONArray featuresArray = new JSONArray();
        for (ServicePoint service_point : service_points) {
            JSONObject feature = new JSONObject();

            feature.put("name", service_point.getName());
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());

            JSONObject geometry = new JSONObject();
            JSONArray coordinates = new JSONArray();
            coordinates.put(service_point.location.lng);
            coordinates.put(service_point.location.lat);
            geometry.put("coordinates", coordinates);
            geometry.put("type", "Point");
            feature.put("geometry", geometry);

            featuresArray.put(feature);
        }
        response.put("features", featuresArray);

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/restricted-areas-geo")
    public ResponseEntity<String> getRestrictedAreas() throws JSONException {
        RestrictedArea[] restricted_areas =
                restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        JSONObject response = new JSONObject();
        response.put("type", "FeatureCollection");

        JSONArray featuresArray = new JSONArray();
        for (RestrictedArea restricted_area : restricted_areas) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());
            JSONObject geometry = new JSONObject();
            JSONArray coordinates = new JSONArray();
            JSONArray inner_coordinates = new JSONArray();
            for (LngLatAlt lnglatalt : restricted_area.getVertices()) {
                JSONArray coordinate = new JSONArray();
                coordinate.put(lnglatalt.lng);
                coordinate.put(lnglatalt.lat);
                inner_coordinates.put(coordinate);
            }
            coordinates.put(inner_coordinates);
            geometry.put("coordinates", coordinates);
            geometry.put("type", "Polygon");
            feature.put("geometry", geometry);
            featuresArray.put(feature);
        }
        response.put("features", featuresArray);

        return ResponseEntity.ok(response.toString());
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

        List<List<LngLat>> paths = new ArrayList<>();

        //paths.add(AStar.AStarPathWithCost(appleton, oceanTerminal, 100, restrictedAreas).first);
        paths.add(AStar.AStarPathWithCost(appleton, brightonStreet, 100, restrictedAreas).first);
        paths.add(AStar.AStarPathWithCost(appleton, annoyingPlace, 100, restrictedAreas).first);
        paths.add(AStar.AStarPathWithCost(appleton, annoyingPlace2, 100, restrictedAreas).first);
        //paths.add(AStar.AStarPathWithCost(brightonStreet, oceanTerminal, 100, restrictedAreas).first);

        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(paths, restrictedAreas));
    }
}
