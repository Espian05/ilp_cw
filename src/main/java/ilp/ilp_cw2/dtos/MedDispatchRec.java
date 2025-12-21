package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MedDispatchRec {
    // ID of the request
    @JsonProperty(value = "id")
    private int id;

    @JsonProperty(value = "date")
    private LocalDate date;

    @JsonProperty(value = "time")
    private LocalTime time;

    @JsonProperty(value = "requirements")
    private Requirements requirements;

    @JsonProperty(value = "delivery")
    private LngLat delivery;

    @JsonIgnore
    public boolean isDateNull() {
        return date == null;
    }

    @JsonIgnore
    public boolean isTimeNull() {
        return time == null;
    }
}
