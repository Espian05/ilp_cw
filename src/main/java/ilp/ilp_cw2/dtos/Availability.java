package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class Availability {
    @JsonProperty(value = "dayOfWeek")
    private DayOfWeek dayOfWeek;

    @JsonProperty(value = "from")
    private LocalTime from;

    @JsonProperty(value = "until")
    private LocalTime until;
}
