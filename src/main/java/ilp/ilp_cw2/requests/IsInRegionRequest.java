package ilp.ilp_cw2.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IsInRegionRequest {
    @JsonProperty(value = "position")
    public final LngLat position;

    @JsonProperty(value = "region")
    public final Region region;

    @JsonCreator
    public IsInRegionRequest(LngLat position, Region region) {
        this.position = position;
        this.region = region;
    }

    /**
     * Returns whether this request has valid members or not
     * @return Validity
     */
    @JsonIgnore
    public boolean isValid() {
        if (position == null || region == null) return false;
        if (!region.isValid() || !position.isValid()) return false;

        return true;
    }
}
