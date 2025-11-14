package ilp.ilp_cw2;

import ilp.ilp_cw2.requests.LngLatPairRequest;
import ilp.ilp_cw2.types.LngLat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DistanceToTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/distanceTo";
    }

    @Test
    void CorrectResponse_ValidPost_Returns1() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(0.0, 1.0));
        assert(restTemplate.postForObject(url, req, Double.class) == 1);
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsStatus200() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(1.0, 1.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 200);
    }

    @Test
    void ErrorResponse_LargeLat_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 91.), new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallLat_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, -91), new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_LargeLng_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(181.0, 0.0), new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallerLng_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(-181.0, 0.0), new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_OneNull_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(null, new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_BothNull_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(null, null);
        assert(restTemplate.postForEntity(url, req, Double.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullBody_ReturnsStatus400() {
        assert(restTemplate.postForEntity(url, null, Double.class).getStatusCode().value() == 400);
    }
}
