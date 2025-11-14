package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Time {
    @JsonProperty(value = "hour")
    public int hour;

    @JsonProperty(value = "minute")
    public int minute;

    @JsonProperty(value = "second")
    public int second;

    @JsonProperty(value = "nano")
    public int nano;
}
