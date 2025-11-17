package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AttributeQuery {
    @JsonProperty(value = "attribute")
    private String attribute;

    @JsonProperty(value = "operator")
    private String operator;

    @JsonProperty(value = "value")
    private String value;
}
