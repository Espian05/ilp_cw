package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Capabilities {
    @JsonProperty(value = "cooling")
    private boolean cooling;

    @JsonProperty(value = "heating")
    private boolean heating;

    @JsonProperty(value = "capacity")
    private double capacity;

    @JsonProperty(value = "maxMoves")
    private int maxMoves;

    @JsonProperty(value = "costPerMove")
    private double costPerMove;

    @JsonProperty(value = "costInitial")
    private double costInitial;

    @JsonProperty(value = "costFinal")
    private double costFinal;
}
