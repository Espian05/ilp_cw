package ilp.ilp_cw2.raycasting;

import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.utils.Utils;

public class Raycasting {
    public static class Line {
        public LngLat p1;
        public LngLat p2;

        public Line(LngLat position1, LngLat position2) {
            if (position1.lng > position2.lng) {
                this.p1 = position2;
                this.p2 = position1;
            } else {
                this.p1 = position1;
                this.p2 = position2;
            }
        }
    }

    public static LngLat findIntersectionPoint(Line l1, Line l2) {
        LngLat pt;
        double epsilon = 1e-15;

        double m1 = (l1.p2.lat - l1.p1.lat) / (l1.p2.lng - l1.p1.lng);
        double m2 = (l2.p2.lat - l2.p1.lat) / (l2.p2.lng - l2.p1.lng);

        // If gradients are too similar
        if (Utils.fpcEquality(m1, m2, epsilon)) {
            return null;
        }

        double c1 = l1.p1.lat - (m1 * l1.p1.lng);
        double c2 = l2.p1.lat - (m2 * l2.p1.lng);

        pt = new LngLat(0, 0);
        pt.lng = (c2 - c1) / (m1 - m2);
        pt.lat = m1 * pt.lng + c1;

        if (pt.lng < l1.p1.lng || pt.lng > l1.p2.lng || pt.lng < l2.p1.lng || pt.lng > l2.p2.lng) {
            pt = null;
        }

        return pt;
    }

    public static boolean intersects(Line l1, Line l2) {
        double m1 = (l1.p2.lat - l1.p1.lat) / (l1.p2.lng - l1.p1.lng);
        double m2 = (l2.p2.lat - l2.p1.lat) / (l2.p2.lng - l2.p1.lng);

        double c1 = l1.p1.lat - (m1 * l1.p1.lng);
        double c2 = l2.p1.lat - (m2 * l2.p1.lng);

        double lng = (c2 - c1) / (m1 - m2);

        if (lng < l1.p1.lng) return false;
        if (lng > l1.p2.lng) return false;
        if (lng < l2.p1.lng) return false;
        if (lng > l2.p2.lng) return false;
        return true;
    }
}
