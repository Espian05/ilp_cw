package ilp.ilp_cw2;

import ilp.ilp_cw2.dtos.Drone;
import ilp.ilp_cw2.dtos.RestrictedArea;
import ilp.ilp_cw2.raycasting.Raycasting;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.types.Region;
import ilp.ilp_cw2.utils.AStarIntegerPosition;
import ilp.ilp_cw2.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class AStarTests extends TestTemplate {
    private final String ilpEndpoint;

    @Autowired
    AStarTests(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
    }

    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/calcDeliveryPath";
    }

    @RepeatedTest(10)
    void TestAnglesAreCorrect() {
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        LngLat point1 = Utils.generateRandomPoint();
        LngLat point2 = Utils.generateRandomPoint();

        ArrayList<LngLat> path = AStarIntegerPosition.AStarPathWithCost(point1, point2, 100, lines).first;

        ArrayList<LngLat> correctVectors = new ArrayList<>();
        correctVectors.add(new LngLat(1.5e-4, 0).normalised()); // Due east
        correctVectors.add(new LngLat(1.3858192987669299E-4, 5.740251485476346E-5).normalised()); // Rotated by 22.5
        correctVectors.add(new LngLat(1.0606601717798212E-4, 1.0606601717798211E-4).normalised()); // Rotated by 22.5 * 2
        correctVectors.add(new LngLat(5.740251485476347E-5, 1.3858192987669299E-4).normalised()); // Rotated by 22.5 * 3

        for (int i = 0; i < path.size() - 1; i++) {
            LngLat start = path.get(i);
            LngLat end = path.get(i + 1);
            LngLat directionVector = end.add(start.scale(-1)).normalised();

            double smallValue = 1e-9;
            boolean oneMatch = false;
            for (LngLat v : correctVectors) {
                double dotProduct = directionVector.dotProduct(v);

                // If aligns with the vector exactly
                if (Math.abs(dotProduct - 1) < smallValue) {
                    oneMatch = true;
                    break;
                }

                // If exactly opposite vector
                if (Math.abs(dotProduct + 1) < smallValue) {
                    oneMatch = true;
                    break;
                }

                // If at a right angle with the vector
                if (Math.abs(dotProduct) < smallValue) {
                    oneMatch = true;
                    break;
                }
            }
            assert(oneMatch);
        }
    }

    @RepeatedTest(10)
    void TestDistancesAreCorrect() {
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        LngLat point1 = Utils.generateRandomPoint();
        LngLat point2 = Utils.generateRandomPoint();

        ArrayList<LngLat> path = AStarIntegerPosition.AStarPathWithCost(point1, point2, 100, lines).first;

        for (int i = 0; i < path.size() - 1; i++) {
            LngLat start = path.get(i);
            LngLat end = path.get(i + 1);

            double smallNumber = 1e-9;
            assert(Math.abs(Utils.getDistance(start, end) - 0.00015) < smallNumber);
        }
    }

    @RepeatedTest(10)
    void TestNoRegionIntersections() {
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndpoint + "/restricted-areas", RestrictedArea[].class);
        Region[] regions = Utils.restrictedAreasToRegions(restrictedAreas).toArray(new Region[0]);
        List<Raycasting.Line> lines = Utils.regionsToLines(regions);

        LngLat point1 = Utils.generateRandomPoint();
        LngLat point2 = Utils.generateRandomPoint();

        ArrayList<LngLat> path = AStarIntegerPosition.AStarPathWithCost(point1, point2, 100, lines).first;

        for (int i = 0; i < path.size() - 1; i++) {
            LngLat start = path.get(i);
            LngLat end = path.get(i + 1);

            Raycasting.Line line = new Raycasting.Line(start, end);

            for (Raycasting.Line r : lines) {
                assert(!Raycasting.intersects(line, r));
            }
        }
    }
}
