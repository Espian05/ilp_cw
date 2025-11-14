package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Drone {
    @JsonProperty(value = "name")
    public String name;

    @JsonProperty(value = "id")
    public String id;

    @JsonProperty(value = "capability")
    public Capabilities capability;
}
