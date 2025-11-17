package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.dtos.AttributeQuery;
import ilp.ilp_cw2.dtos.Drone;
import ilp.ilp_cw2.dtos.RestrictedArea;
import ilp.ilp_cw2.dtos.ServicePoint;
import ilp.ilp_cw2.types.LngLatAlt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
}
