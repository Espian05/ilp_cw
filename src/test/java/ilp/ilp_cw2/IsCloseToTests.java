package ilp.ilp_cw2;

import ilp.ilp_cw2.requests.LngLatPairRequest;
import ilp.ilp_cw2.types.LngLat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IsCloseToTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/isCloseTo";
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsTrue() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(0.0, 0.00014));
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsFalse() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(0.0, 0.00015));
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.FALSE);
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsStatus200() {
        LngLatPairRequest req = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(0.0, 0.00014));
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 200);
    }

    @Test
    void ErrorResponse_OneNull_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(null, new LngLat(0.0, 0.0));
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_BothNull_ReturnsStatus400() {
        LngLatPairRequest req = new LngLatPairRequest(null, null);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullBody_ReturnsStatus400() {
        assert(restTemplate.postForEntity(url, null, Boolean.class).getStatusCode().value() == 400);
    }
}
