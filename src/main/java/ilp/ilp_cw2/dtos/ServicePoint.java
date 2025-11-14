package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLatAlt;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServicePoint {
    @JsonProperty(value = "name")
    public String name;

    @JsonProperty(value = "id")
    public int id;

    @JsonProperty(value = "location")
    public LngLatAlt location;

    @JsonCreator
    public ServicePoint(String name, int id, LngLatAlt location) {
        this.name = name;
        this.id = id;
        this.location = location;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "ServicePoint [name=" + name + ", id=" + id + ", location=" + location + "]";
    }
}
