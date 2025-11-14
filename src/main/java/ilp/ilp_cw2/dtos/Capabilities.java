package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Capabilities {
    @JsonProperty(value = "cooling")
    public boolean cooling;

    @JsonProperty(value = "heating")
    public boolean heating;

    @JsonProperty(value = "capacity")
    public double capacity;

    @JsonProperty(value = "maxMoves")
    public int maxMoves;

    @JsonProperty(value = "costPerMove")
    public double costPerMove;

    @JsonProperty(value = "costInitial")
    public double costInitial;

    @JsonProperty(value = "costFinal")
    public double costFinal;
}
