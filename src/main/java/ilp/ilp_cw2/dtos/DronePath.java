package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DronePath {
    @JsonProperty(value = "droneId")
    public String droneId;

    @JsonProperty(value = "deliveries")
    public Delivery[] deliveries;
}
