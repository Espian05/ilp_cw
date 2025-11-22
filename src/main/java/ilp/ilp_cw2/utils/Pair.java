package ilp.ilp_cw2.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pair<First, Second> {
    @JsonProperty(value = "first")
    public First first;

    @JsonProperty(value = "second")
    public Second second;

    @JsonCreator
    public Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }
}
