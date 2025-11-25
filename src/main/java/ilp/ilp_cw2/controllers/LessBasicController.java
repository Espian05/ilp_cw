package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.dtos.*;
import ilp.ilp_cw2.raycasting.Raycasting;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.LngLatAlt;
import ilp.ilp_cw2.types.Region;
import ilp.ilp_cw2.utils.AStar;
import ilp.ilp_cw2.utils.GeoJson;
import ilp.ilp_cw2.utils.Pair;
import ilp.ilp_cw2.utils.Utils;
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
import java.util.Timer;

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

        List<Drone> availableDrones = Utils.queryAvailableDrones(medDispatchRecs, drones, dronesForServicePoints, servicePoints);
        return ResponseEntity.ok(availableDrones.stream().map(drone -> drone.id).toList());
    }

    @PostMapping(endpointStart + "/calcDeliveryPath")
    public ResponseEntity<DeliveryPaths> calcDeliveryPath(@RequestBody MedDispatchRec[] medDispatchRecs) {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        // Get available drones
        List<Drone> availableDrones = Utils.queryAvailableDrones(medDispatchRecs, drones, dronesForServicePoints, servicePoints);

        // If no available drones, return an empty object
        if (availableDrones.isEmpty()) {
            return ResponseEntity.ok(new DeliveryPaths());
        }

        // Select my drone
        Drone drone = availableDrones.getFirst();

        // Get list of delivery points from the medDispatchRecs
        List<LngLat> deliveryPoints = Arrays.stream(medDispatchRecs).map(MedDispatchRec::getDelivery).toList();

        // Calculate paths in order of occurrence for now
        List<LngLat> pointsToPathThrough = new ArrayList<>();
        pointsToPathThrough.add(drone.servicePointPosition);
        pointsToPathThrough.addAll(deliveryPoints);
        pointsToPathThrough.add(drone.servicePointPosition);

        // Get the lines from the restricted areas
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        // Calculate paths
        List<List<LngLat>> paths = new ArrayList<>();
        int totalMoves = 0;
        for (int i = 0; i < pointsToPathThrough.size() - 1; i++) {
            List<LngLat> path;
            if (i == 0) path = AStar.AStarPathWithCost(pointsToPathThrough.get(i), pointsToPathThrough.get(i + 1), 100, lines).first;
            else path = AStar.AStarPathWithCost(paths.getLast().getLast(), pointsToPathThrough.get(i + 1), 100, lines).first;
            path.add(path.getLast());
            paths.add(path);
            totalMoves += path.size();
        }

        // Convert paths to delivery paths
        DeliveryPaths deliveryPaths = new DeliveryPaths();

        deliveryPaths.totalCost = totalMoves * drone.capability.getCostPerMove() + drone.capability.getCostInitial() + drone.capability.getCostFinal();
        deliveryPaths.totalMoves = totalMoves;

        List<DronePath> dronePaths = new ArrayList<>();
        DronePath dronePath = new DronePath();
        dronePath.droneId = drone.id;

        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Delivery delivery = new Delivery();
            if (i < medDispatchRecs.length) delivery.deliveryId = medDispatchRecs[i].getId();
            else delivery.deliveryId = null;
            delivery.flightPath = paths.get(i).toArray(new LngLat[0]);
            deliveries.add(delivery);
        }
        dronePath.deliveries = deliveries.toArray(new Delivery[0]);

        dronePaths.add(dronePath);

        deliveryPaths.dronePaths = dronePaths.toArray(new DronePath[0]);

        return ResponseEntity.ok(deliveryPaths);
    }

    @PostMapping(endpointStart + "/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody MedDispatchRec[] medDispatchRecs) throws JSONException {
        Drone[] drones = restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
        DronesForServicePoint[] dronesForServicePoints = restTemplate.getForObject(ilpEndpoint + "/drones-for-service-points", DronesForServicePoint[].class);
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        // Get available drones
        List<Drone> availableDrones = Utils.queryAvailableDrones(medDispatchRecs, drones, dronesForServicePoints, servicePoints);

        // If no available drones, return an empty object
        if (availableDrones.isEmpty()) {
            return ResponseEntity.ok("");
        }

        // Select my drone
        Drone drone = availableDrones.getFirst();

        // Get the lines from the restricted areas
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        // Get list of delivery points from the medDispatchRecs
        List<LngLat> deliveryPoints = Arrays.stream(medDispatchRecs).map(MedDispatchRec::getDelivery).toList();

        List<LngLat> bestPath = Utils.bestDeliveryOrderingPathAStar(drone.servicePointPosition, deliveryPoints, lines).second;
        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(new ArrayList<>(){{add(bestPath);}}, restrictedAreas));

        // Calculate paths in order of occurrence for now
        /*
        List<LngLat> pointsToPathThrough = new ArrayList<>();
        pointsToPathThrough.add(drone.servicePointPosition);
        pointsToPathThrough.addAll(deliveryPoints);
        pointsToPathThrough.add(drone.servicePointPosition);

        // Calculate paths
        List<List<LngLat>> paths = new ArrayList<>();
        for (int i = 0; i < pointsToPathThrough.size() - 1; i++) {
            List<LngLat> path;
            if (i == 0) path = AStar.AStarPathWithCost(pointsToPathThrough.get(i), pointsToPathThrough.get(i + 1), 100, lines).first;
            else path = AStar.AStarPathWithCost(paths.getLast().getLast(), pointsToPathThrough.get(i + 1), 100, lines).first;
            paths.add(path);
        }

        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(paths, restrictedAreas));
        */
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

        List<LngLat> deliveryPoints = new ArrayList<>();
        deliveryPoints.add(brightonStreet);
        deliveryPoints.add(annoyingPlace);
        deliveryPoints.add(annoyingPlace2);
        deliveryPoints.add(testPoint1);
        deliveryPoints.add(testPoint2);
        deliveryPoints.add(hafsas);
        //deliveryPoints.add(oceanTerminal);
        //deliveryPoints.add(hamilton);

        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);


        List<LngLat> bestPath = Utils.bestDeliveryOrderingPathEuclidean(appleton, deliveryPoints, lines).second;
        return ResponseEntity.ok(GeoJson.toGeoJsonWithRegions(new ArrayList<>(){{add(bestPath);}}, restrictedAreas));
    }
}
