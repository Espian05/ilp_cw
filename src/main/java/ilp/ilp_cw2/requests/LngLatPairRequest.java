package ilp.ilp_cw2.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LngLatPairRequest {
    @JsonProperty(value = "position1")
    public LngLat position1;

    @JsonProperty(value = "position2")
    public LngLat position2;

    @JsonCreator
    public LngLatPairRequest(LngLat position1, LngLat position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    /**
     * Returns whether this request has valid members or not
     * @return Validity
     */
    @JsonIgnore
    public boolean isValid() {
        if (position1 == null || position2 == null) return false;
        return position1.isValid() && position2.isValid();
    }
}
