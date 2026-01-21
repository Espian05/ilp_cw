package ilp.ilp_cw2;

import ilp.ilp_cw2.dtos.*;
import ilp.ilp_cw2.types.LngLat;
import ilp.ilp_cw2.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class CalcDeliveryPathTests extends TestTemplate {
    private final String ilpEndpoint;

    @Autowired
    CalcDeliveryPathTests(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
    }

    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/calcDeliveryPath";
    }

    /**
     * Generates a number of dispatch requests in a rectangle roughly over Edinburgh
     * @param numberOfDispatchRecs The number of dispatch recs to generate
     * @return The dispatch recs
     */
    ArrayList<MedDispatchRec> generateRandomDispatchRecs(int numberOfDispatchRecs) {
        ArrayList<LngLat> randomPoints = Utils.generateRandomPoints(numberOfDispatchRecs);

        ArrayList<MedDispatchRec> dispatchRecs = new ArrayList<>();
        for (int i = 0; i < numberOfDispatchRecs; i++) {
            MedDispatchRec newMedDispatchRec = MedDispatchRec.builder()
                    .id(0)
                    .date(LocalDate.of(2026, 1, 15))
                    .time(LocalTime.of(15, 0))
                    .requirements(Requirements.builder().capacity(0.0).cooling(false).heating(false).build())
                    .delivery(randomPoints.get(i))
                    .build();

            dispatchRecs.add(newMedDispatchRec);
        }
        return dispatchRecs;
    }

    @RepeatedTest(10)
    void QueryTakesLessThan30Seconds(RepetitionInfo repetitionInfo) {
        long startTime = System.currentTimeMillis();

        ArrayList<MedDispatchRec> req = generateRandomDispatchRecs(9);
        restTemplate.postForEntity(url, req, DeliveryPaths.class).getBody();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Test " + repetitionInfo.getCurrentRepetition() + " of " + repetitionInfo.getTotalRepetitions());
        System.out.println("Test took: " + duration / 1000.0 + "s");
        assert(duration < 30000);
    }

    @RepeatedTest(10)
    void DeliversAllMedDispatchRecs(RepetitionInfo repetitionInfo) {
        ArrayList<MedDispatchRec> req = generateRandomDispatchRecs(5);
        DeliveryPaths deliveryPaths = restTemplate.postForEntity(url, req, DeliveryPaths.class).getBody();

        // If no delivery paths, assume it was impossible and return
        // (not part of this test to check if it was possible)
        if (deliveryPaths == null) {
            System.out.println("No delivery paths found for test " + repetitionInfo.getCurrentRepetition());
            return;
        }

        // Remove any medDispatches that have been delivered
        for (DronePath dronePath : deliveryPaths.dronePaths) {
            for  (Delivery delivery : dronePath.deliveries) {
                LngLat lastPos = delivery.flightPath.getLast();
                LngLat secondLastPos = delivery.flightPath.get(delivery.flightPath.size() - 2);
                assert(lastPos.equals(secondLastPos));
                for (MedDispatchRec medDispatchRec : req) {
                    if (Utils.isClose(lastPos, medDispatchRec.getDelivery())) {
                        req.remove(medDispatchRec);
                        break;
                    }
                }
            }
        }

        assert(req.isEmpty());
    }

    @RepeatedTest(10)
    void PathStartsAndEndsAtSameServicePoint() {
        // Get service points to check against
        ServicePoint[] servicePoints = restTemplate.getForObject(ilpEndpoint + "/service-points", ServicePoint[].class);

        ArrayList<MedDispatchRec> req = generateRandomDispatchRecs(5);
        DeliveryPaths deliveryPaths = restTemplate.postForEntity(url, req, DeliveryPaths.class).getBody();

        // If no path returned, assume correct (path must not have been possible)
        if (deliveryPaths == null) return;

        // Check all individual drone delivery paths
        for (DronePath dronePath : deliveryPaths.dronePaths) {
            // If the distance between the start and end points is near the same
            // service point, this is correct
            LngLat startPoint = dronePath.deliveries[0].flightPath.getFirst();
            int lastDeliveryIndex = dronePath.deliveries.length - 1;
            LngLat endPoint = dronePath.deliveries[lastDeliveryIndex].flightPath.getLast();

            boolean bothClose = false;
            for (ServicePoint servicePoint : servicePoints) {
                if (!Utils.isClose(servicePoint.location, startPoint)) continue;
                if (!Utils.isClose(servicePoint.location, endPoint)) continue;
                bothClose = true;
                break;
            }
            assert(bothClose);
        }
    }
}
