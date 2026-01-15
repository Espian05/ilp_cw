package ilp.ilp_cw2;

import ilp.ilp_cw2.dtos.DeliveryPaths;
import ilp.ilp_cw2.dtos.MedDispatchRec;
import ilp.ilp_cw2.dtos.Requirements;
import ilp.ilp_cw2.types.LngLat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

public class CalcDeliveryPathTests extends TestTemplate {
    @BeforeEach
    protected void setEndpoint() {
        url += "/api/v1/calcDeliveryPathAsGeoJson";
    }

    @RepeatedTest(100)
    void QueryTakesLessThan30Seconds() {
        long startTime = System.currentTimeMillis();

        ArrayList<MedDispatchRec> req = getRandomDispatchRecs(5);
        String geoJson = restTemplate.postForEntity(url, req, String.class).getBody();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Duration: " + duration / 1000.0 + "s");
        System.out.println(geoJson);
        assert(duration < 30000);
    }

    ArrayList<MedDispatchRec> getRandomDispatchRecs(int numberOfDispatchRecs) {
        // Ranges defining a rectangle of Edinburgh for test points
        double minLng = -3.30232720254088;
        double maxLng = -3.07069927106582;
        double lngDifference = maxLng - minLng;

        double minLat = 55.902444794164666;
        double maxLat = 55.99505311585057;
        double latDifference = maxLat - minLat;

        ArrayList<MedDispatchRec> dispatchRecs = new ArrayList<>();
        for (int i = 0; i < numberOfDispatchRecs; i++) {
            double randomLng = minLng + Math.random() * lngDifference;
            double randomLat = minLat + Math.random() * latDifference;
            LngLat randomLngLat = new LngLat(randomLng, randomLat);

            System.out.println("Random LngLat: " + randomLngLat);

            MedDispatchRec newMedDispatchRec = MedDispatchRec.builder()
                    .id(0)
                    .date(LocalDate.of(2026, 1, 15))
                    .time(LocalTime.of(15, 0))
                    .requirements(Requirements.builder().capacity(0.0).cooling(false).heating(false).build())
                    .delivery(randomLngLat)
                    .build();

            dispatchRecs.add(newMedDispatchRec);
        }
        return dispatchRecs;
    }
}
