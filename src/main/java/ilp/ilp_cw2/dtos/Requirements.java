package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Requirements {
    @JsonProperty(value = "capacity")
    private Double capacity;

    @JsonProperty(value = "cooling")
    private Boolean cooling;

    @JsonProperty(value = "heating")
    private Boolean heating;

    @JsonProperty(value = "maxCost")
    private Double maxCost;

    @JsonIgnore
    public boolean maxCostIsNull() {
        return maxCost == null;
    }
}
