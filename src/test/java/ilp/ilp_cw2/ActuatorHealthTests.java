package ilp.ilp_cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ActuatorHealthTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/actuator/health";
    }

    @Test
    void CorrectResponse_ValidGet_ReturnsUp() {
        assert(restTemplate.getForObject(url, String.class).contains("UP"));
    }

    @Test
    void CorrectResponse_ValidGet_ReturnsStatus200() {
        assert(restTemplate.getForEntity(url, String.class).getStatusCode().value() == 200);
    }
}
