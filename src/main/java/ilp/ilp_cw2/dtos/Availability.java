package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Availability {
    @JsonProperty(value = "dayOfWeek")
    public String dayOfWeek;

    @JsonProperty(value = "from")
    public Time from;

    @JsonProperty(value = "until")
    public Time until;
}
