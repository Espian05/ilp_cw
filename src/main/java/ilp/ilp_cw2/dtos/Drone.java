package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;
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

    @JsonIgnore
    public LngLat servicePointPosition;

    @JsonIgnore
    public Double estimatedCost;
}
