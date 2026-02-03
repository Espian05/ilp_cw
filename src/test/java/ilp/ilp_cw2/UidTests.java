package ilp.ilp_cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UidTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/uid";
    }

    @Test
    public void CorrectResponse_ValidGet_ReturnsUid() {
        assert(restTemplate.getForObject(url, String.class).contains("s2571964"));
    }

    @Test
    public void CorrectResponse_ValidGet_ReturnsStatus200() {
        assert(restTemplate.getForEntity(url, String.class).getStatusCode().value() == 200);
    }

    // A comment for stuart
}
