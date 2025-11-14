package ilp.ilp_cw2.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LngLatAlt extends LngLat {
    @JsonProperty(value = "alt")
    public double alt;

    @JsonCreator
    public LngLatAlt(double lng, double lat, double alt) {
        super(lng, lat);
        this.alt = alt;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "{" + lng + ", " + lat + ", " + alt + "}";
    }
}
