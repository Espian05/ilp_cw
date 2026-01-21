package ilp.ilp_cw2;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class TestTemplate {
    @LocalServerPort
    protected int port;

    protected final TestRestTemplate restTemplate = new TestRestTemplate();

    protected String url;

    @BeforeEach
    void Init() {
        url = "http://localhost:" + port;
    }

    @BeforeEach
    protected abstract void setEndpoint();

    /*
    General testing strategy (for me)
     - Valid input -> correct output value
        (if boolean output, test for both types. If numerical, just one for now)
     - Valid input -> correct output code
     - Test anything that would result in an "out of range" or "invalid value error"
        (basically anything that makes an internal "isValid" call return false)
     - Test passing null values wherever possible
    */
}
