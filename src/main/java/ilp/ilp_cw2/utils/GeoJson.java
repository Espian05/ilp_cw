package ilp.ilp_cw2.utils;

import ilp.ilp_cw2.dtos.RestrictedArea;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.LngLatAlt;
import ilp.ilp_cw2.types.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class GeoJson {
    public static String toGeoJson(List<List<LngLat>> pointsLists) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("type", "FeatureCollection");

        JSONArray featuresArray = new JSONArray();

        for (List<LngLat> points : pointsLists) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());

            JSONObject geometry = new JSONObject();
            JSONArray coordinates = new JSONArray();
            for (LngLat point : points) {
                JSONArray coordinate = new JSONArray();
                coordinate.put(point.lng);
                coordinate.put(point.lat);
                coordinates.put(coordinate);
            }
            geometry.put("coordinates", coordinates);
            geometry.put("type", "LineString");
            feature.put("geometry", geometry);
            featuresArray.put(feature);
        }

        response.put("features", featuresArray);

        return response.toString();
    }

    public static String toGeoJsonWithRegions(ArrayList<ArrayList<LngLat>> pointsLists, RestrictedArea[] restrictedAreas) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("type", "FeatureCollection");
        JSONArray featuresArray = new JSONArray();

        // Restricted areas
        for (RestrictedArea restrictedArea : restrictedAreas) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());

            JSONObject geometry = new JSONObject();
            JSONArray coordinates = new JSONArray();
            JSONArray inner_coordinates = new JSONArray();
            for (LngLatAlt lnglatalt : restrictedArea.getVertices()) {
                JSONArray coordinate = new JSONArray();
                coordinate.put(lnglatalt.lng);
                coordinate.put(lnglatalt.lat);
                inner_coordinates.put(coordinate);
            }
            coordinates.put(inner_coordinates);
            geometry.put("coordinates", coordinates);
            geometry.put("type", "Polygon");
            feature.put("geometry", geometry);
            featuresArray.put(feature);
        }

        // Drone paths

        for (List<LngLat> points : pointsLists) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());

            JSONObject geometry = new JSONObject();
            JSONArray coordinates = new JSONArray();
            for (LngLat point : points) {
                JSONArray coordinate = new JSONArray();
                coordinate.put(point.lng);
                coordinate.put(point.lat);
                coordinates.put(coordinate);
            }
            geometry.put("coordinates", coordinates);
            geometry.put("type", "LineString");
            feature.put("geometry", geometry);
            featuresArray.put(feature);
        }

        response.put("features", featuresArray);

        return response.toString();
    }
}
