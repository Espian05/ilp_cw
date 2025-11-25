package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLatAlt;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RestrictedArea {
    @JsonProperty(value = "name")
    String name;

    @JsonProperty(value = "id")
    int id;

    @JsonProperty(value = "limits")
    Limits limits;

    @JsonProperty(value = "vertices")
    List<LngLatAlt> vertices;


}
