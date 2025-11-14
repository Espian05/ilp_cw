package ilp.ilp_cw2.controllers;

import ilp.ilp_cw2.config.Config;
import ilp.ilp_cw2.requests.IsInRegionRequest;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.requests.LngLatPairRequest;
import ilp.ilp_cw2.requests.NextPositionRequest;
import ilp.ilp_cw2.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class BasicController {
    private final String endpointStart = "/api/v1";

    private final RestTemplate restTemplate = new RestTemplate();

    private final String ilpEndpoint;

    @Autowired
    BasicController(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
    }

    @GetMapping(endpointStart + "/uid")
    public String uid() {
        return "s2571964";
    }

    @PostMapping(endpointStart + "/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody(required = false) LngLatPairRequest req) {
        if (req == null || !req.isValid()) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(Utils.getDistance(req.position1, req.position2));
    }

    @PostMapping(endpointStart + "/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody(required = false) LngLatPairRequest req) {
        if (req == null || !req.isValid()) {
            return ResponseEntity.badRequest().body(false);
        }

        return ResponseEntity.ok(Utils.isClose(req.position1, req.position2));
    }

    @PostMapping(endpointStart + "/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody(required = false) NextPositionRequest req) {
        if (req == null || !req.isValid()) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(Utils.getNextPosition(req.start, req.angle));
    }

    @PostMapping(endpointStart + "/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody(required = false) IsInRegionRequest req) {
        if (req == null || !req.isValid()) {
            return ResponseEntity.badRequest().body(false);
        }

        return ResponseEntity.ok(Utils.isInRegion(req.region, req.position));
    }
}
