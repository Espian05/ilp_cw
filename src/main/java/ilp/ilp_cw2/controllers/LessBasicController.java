package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.dtos.RestrictedArea;
import ilp.ilp_cw2.dtos.ServicePoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class LessBasicController {
    private final String endpointStart = "/api/v1";

    private final RestTemplate restTemplate = new RestTemplate();

    private final String ilpEndpoint;

    @Autowired
    LessBasicController(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
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

    @GetMapping("restricted-areas-geo")
    public ResponseEntity<String> getRestrictedAreas() throws JSONException {
        RestrictedArea[] restricted_areas =
                restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);

        JSONObject response = new JSONObject();
        response.put("type", "FeatureCollection");


        return ResponseEntity.ok(response.toString());
    }
}
