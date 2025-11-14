package ilp.ilp_cw2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public String ilpEndpoint() {
        return System.getenv().getOrDefault("ILP_ENDPOINT", "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/");
    }
}
