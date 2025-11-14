package ilp.ilp_cw2.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;

public class Utils {
    /**
     * Returns the euclidean distance between two positions
     * (because otherwise I'm redoing checks I *know* have to have happened in my implementation)
     * @param position1 The first position
     * @param position2 The second position
     * @return double distance between the points
     */
    public static double getDistance(LngLat position1, LngLat position2) {
        return Math.sqrt(Math.pow(position1.lat - position2.lat, 2) +
                Math.pow(position1.lng - position2.lng, 2));
    }

    /**
     * Returns whether the distance between two points is below a given threshold
     * (defined by the docs is 0.00015)
     * @param position1 The first position
     * @param position2 The second position
     * @return boolean
     */
    public static boolean isClose(LngLat position1, LngLat position2) {
        return Math.abs(getDistance(position1, position2)) < 0.00015;
    }

    /**
     * Returns the next position from a given point at a given angle,
     * defined in the docs as a 0.00015 move in the given direction
     * (could be made more efficient using a lookup table, given
     *  the small amount of possible angles)
     * @param position The position to start from
     * @param angle The direction to move in
     * @return The next position
     */
    public static LngLat getNextPosition(LngLat position, double angle) {
        double radians = Math.toRadians(angle);
        LngLat offset = new LngLat(Math.cos(radians), Math.sin(radians));
        return position.add(offset.scale(0.00015));
    }

    /**
     * Returns whether this request's .position is inside this request's region
     * @return Whether the point is inside or outside
     */
    public static boolean isInRegion(Region region, LngLat position) {
        boolean inRegion = false;

        for (int i = 0; i < region.vertices.size() - 1; i++) {
            LngLat position1 = region.vertices.get(i);
            LngLat position2 = region.vertices.get(i + 1);

            // As defined in the doc, if the point is on an edge it is counted as "inside"
            if (checkIfOnLine(position1, position2, position)) return true;

            // If it intersects an even number it's outside, odd number then inside
            // achieved by flipping a boolean initially set to false
            if (intersects(position1, position2, position)) {
                inRegion = !inRegion;
            }
        }

        return inRegion;
    }

    /**
     * Checks if a point is on a line by first checking collinearity,
     * and then checking within the bounding box defined by the points
     * @param position1 The first corner
     * @param position2 The opposite corner
     * @param point The point to check
     * @return Whether the point is on the line
     */
    private static boolean checkIfOnLine(LngLat position1, LngLat position2, LngLat point) {
        if (getGradient(point, position1) == getGradient(point, position2)) {
            return point.lng <= Math.max(position1.lng, position2.lng) &&
                    point.lat <= Math.max(position1.lat, position2.lat) &&
                    point.lng >= Math.min(position1.lng, position2.lng) &&
                    point.lat >= Math.min(position1.lat, position2.lat);
        }
        return false;
    }

    /**
     * Checks whether a ray going from point to infinity off to the right
     * will intersect with the line from position1 to position2 using this
     * quite neat algorithm I found here:
     * https://rosettacode.org/wiki/Ray-casting_algorithm
     * Don't worry, I do actually understand it I didn't just copy it!
     * @param position1 The lower position
     * @param position2 The upper position
     * @param point The point to cast from
     * @return Whether the ray intersects the line
     */
    private static boolean intersects(LngLat position1, LngLat position2, LngLat point) {
        // Ensure that the first position is the lower of the two
        if (position1.lat > position2.lat) {
            return intersects(position2, position1, point);
        }

        // Essentially a bounds check, if the point is to the left, the ray must always collide
        // if it's above, below, or to the right, it can never collide
        if (point.lat > position2.lat) return false;
        if (point.lat < position1.lat) return false;
        if (point.lng > Math.max(position1.lng, position2.lng)) return false;
        if (point.lng < Math.min(position1.lng, position2.lng)) return true;

        // Otherwise, we check the gradients
        double mPosition2 = getGradient(position1, position2);
        double mPoint = getGradient(position1, point);

        return mPoint < mPosition2;
    }

    /**
     * Computes the gradient between two points
     * Specifically returns positive infinity if the lats are the same
     * because it works for my algorithm - this is not supposed to be general
     * @param position1 The first position
     * @param position2 The second position
     * @return The gradient
     */
    private static double getGradient(LngLat position1, LngLat position2) {
        if (position1.lng == position2.lng) return Double.POSITIVE_INFINITY;
        return (position1.lat - position2.lat) / (position1.lng - position2.lng);
    }
}
