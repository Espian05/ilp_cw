package ilp.ilp_cw2.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DeliveryPaths {
    @JsonProperty(value = "totalCost")
    public double totalCost;

    @JsonProperty(value = "totalMoves")
    public int totalMoves;

    @JsonProperty(value = "dronePaths")
    public ArrayList<DronePath> dronePaths;
}
