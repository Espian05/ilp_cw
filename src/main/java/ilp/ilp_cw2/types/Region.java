package ilp.ilp_cw2.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class Region {
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "vertices", required = true)
    public ArrayList<LngLat> vertices;

    @JsonCreator
    public Region(String name, ArrayList<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    /**
     * Returns whether this type has valid members or not
     * @return Validity
     */
    @JsonIgnore
    public boolean isValid() {
        if (vertices.size() < 4) return false;
        if (!vertices.getFirst().equals(vertices.getLast())) return false;

        return true;
    }
}
