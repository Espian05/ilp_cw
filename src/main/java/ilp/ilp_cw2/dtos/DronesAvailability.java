package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DronesAvailability {
    @JsonProperty(value = "id")
    public String id;

    @JsonProperty(value = "availability")
    public Availability[] availability;
}
