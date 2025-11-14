package ilp.ilp_cw2;

import ilp.ilp_cw2.requests.IsInRegionRequest;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class IsInRegionTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/isInRegion";
    }

    private final Region validRegion = Region.builder().name("test").vertices(
            new ArrayList<>(Arrays.asList(
                    new LngLat(-1.0, -1.0),
                    new LngLat(-1.0, 1.0),
                    new LngLat(1.0, 1.0),
                    new LngLat(1.0, -1.0),
                    new LngLat(-1.0, -1.0)
            ))).build();

    @Test
    void CorrectResponse_ValidPost_ReturnsTrue() {
        LngLat lnglat = new LngLat(0.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsFalse() {
        LngLat lnglat = new LngLat(2.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.FALSE);
    }

    @Test
    void CorrectResponse_ValidPost_ReturnsStatus200() {
        LngLat lnglat = new LngLat(0.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 200);
    }

    @Test
    void CorrectResponse_OnTopLine_ReturnsTrue() {
        LngLat lnglat = new LngLat(1.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_OnBottomLine_ReturnsTrue() {
        LngLat lnglat = new LngLat(-1.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_OnLeftLine_ReturnsTrue() {
        LngLat lnglat = new LngLat(0.0, -1.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_OnRightLine_ReturnsTrue() {
        LngLat lnglat = new LngLat(0.0, 1.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void CorrectResponse_OnTopLeftCorner_ReturnsTrue() {
        LngLat lnglat = new LngLat(-1.0, -1.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForObject(url, req, Boolean.class) == Boolean.TRUE);
    }

    @Test
    void ErrorResponse_LargeLng_ReturnsStatus400() {
        LngLat lnglat = new LngLat(181.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallLng_ReturnsStatus400() {
        LngLat lnglat = new LngLat(-181.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_LargeLat_ReturnsStatus400() {
        LngLat lnglat = new LngLat(0.0, 91.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallLat_ReturnsStatus400() {
        LngLat lnglat = new LngLat(0.0, -91.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_UnclosedRegion_ReturnsStatus400() {
        LngLat lnglat = new LngLat(0.0, 0.0);
        Region invalidRegion = validRegion;
        invalidRegion.vertices.removeFirst();
        IsInRegionRequest req = new IsInRegionRequest(lnglat, invalidRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_SmallRegion_ReturnsStatus400() {
        LngLat lnglat = new LngLat(0.0, 0.0);
        Region invalidRegion = Region.builder().name("test").vertices(
                new ArrayList<>(Arrays.asList(
                        new LngLat(-1.0, -1.0),
                        new LngLat(-1.0, 1.0),
                        new LngLat(-1.0, -1.0)
                ))).build();
        IsInRegionRequest req = new IsInRegionRequest(lnglat, invalidRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullLngLat_ReturnsStatus400() {
        IsInRegionRequest req = new IsInRegionRequest(null, validRegion);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullRegion_ReturnsStatus400() {
        LngLat lnglat = new LngLat(0.0, 0.0);
        IsInRegionRequest req = new IsInRegionRequest(lnglat, null);
        assert(restTemplate.postForEntity(url, req, Boolean.class).getStatusCode().value() == 400);
    }

    @Test
    void ErrorResponse_NullBody_ReturnsStatus400() {
        assert(restTemplate.postForEntity(url, null, Boolean.class).getStatusCode().value() == 400);
    }
}
