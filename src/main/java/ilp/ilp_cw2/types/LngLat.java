package ilp.ilp_cw2.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class LngLat {
    @JsonProperty(value = "lng")
    public double lng;

    @JsonProperty(value = "lat")
    public double lat;

    @JsonCreator
    public LngLat(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    /**
     * Returns whether this type has valid members or not
     * @return Validity
     */
    @JsonIgnore
    public boolean isValid() {
        // Assumption that as longitude and latitude are measured in degrees,
        // the bound is -180 to +180
        if (lng < -180 || lng > 180) return false;
        if (lat < -90 || lat > 90) return false;
        return true;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "{" + lng + ", " + lat + "}";
    }

    /**
     * Returns a new LngLat scaled by the given scalar
     * @param scalar The amount to scale by
     * @return The scaled LngLat
     */
    @JsonIgnore
    public LngLat scale(double scalar) {
        return new LngLat(lng * scalar, lat * scalar);
    }

    /**
     * Returns a new LngLat that is the result of this LngLat
     * added to another
     * @param other The other LngLat to add
     * @return The sum
     */
    @JsonIgnore
    public LngLat add(LngLat other) {
        return new LngLat(lng + other.lng, lat + other.lat);
    }

    @JsonIgnore
    public void staticAdd(LngLat other) {
        this.lng += other.lng;
        this.lat += other.lat;
    }

    @JsonIgnore
    public void fastStaticAdd(double lng, double lat) {
        this.lng += lng;
        this.lat += lat;
    }
}
