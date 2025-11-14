package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Limits {
    @JsonProperty(value = "lower")
    public int lower;

    @JsonProperty(value = "upper")
    public int upper;

    @JsonCreator
    public Limits(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }
}
