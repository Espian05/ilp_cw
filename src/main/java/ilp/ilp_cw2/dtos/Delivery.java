package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import ilp.ilp_cw2.types.LngLat;

import java.util.ArrayList;

public class Delivery {
    @JsonProperty(value = "deliveryId")
    public Integer deliveryId;

    @JsonProperty(value = "flightPath")
    public ArrayList<LngLat> flightPath;
}
