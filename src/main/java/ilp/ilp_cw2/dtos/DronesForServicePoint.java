package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DronesForServicePoint {
    @JsonProperty(value = "servicePointId")
    public int servicePointId;

    @JsonProperty(value = "drones")
    public DronesAvailability[] drones;
}
