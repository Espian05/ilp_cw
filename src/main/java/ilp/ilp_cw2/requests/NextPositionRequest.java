package ilp.ilp_cw2.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;

public class NextPositionRequest {
    @JsonProperty(value = "start")
    public LngLat start;

    @JsonProperty(value = "angle")
    public double angle;

    @JsonCreator
    public NextPositionRequest(LngLat start, double angle) {
        this.start = start;
        this.angle = angle;
    }

    /**
     * Returns whether this request has valid members or not
     * @return Validity
     */
    @JsonIgnore
    public boolean isValid() {
        if (start == null) return false;
        if (!start.isValid()) return false;
        if (angle < 0 || angle > 360) return false;
        if (angle % 22.5 != 0) return false;
        return true;
    }
}
