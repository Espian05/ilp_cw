package ilp.ilp_cw2;

import ilp.ilp_cw2.requests.NextPositionRequest;
import ilp.ilp_cw2.types.LngLat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NextPositionTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/nextPosition";
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsCorrectLngLat() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 0.0), 0.0);
        assert(restTemplate.postForObject(url, req, LngLat.class).equals(new LngLat(0.00015, 0.0)));
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsStatus200() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 0.0), 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 200);
    }

    @Test
    void ErrorResponse_LargeLat_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 91.0), 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallLat_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, -91.0), 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_LargeLng_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(181.0, 0.0), 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallerLng_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(-181.0, 0.0), 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_LargeAngle_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 0.0), 361.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_InvalidAngle_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 0.0), 22.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallAngle_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(new LngLat(0.0, 0.0), -1.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullLngLat_ReturnsStatus400() {
        NextPositionRequest req = new NextPositionRequest(null, 0.0);
        assert(restTemplate.postForEntity(url, req, LngLat.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullBody_ReturnsStatus400() {
        assert(restTemplate.postForEntity(url, null, LngLat.class).getStatusCode().value() == 400);
    }
}
