package cgeo.geocaching.models;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.utils.formulas.Value;

import org.junit.Test;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class CalculatedCoordinateTest {

    @Test
    public void basicCalculations() {
        final CalculatedCoordinate cc = CalculatedCoordinate.createFromConfig("{CC|N48 12.A45|E13 8.67B}");
        assertThat(cc.getLatitudePattern()).isEqualTo("N48 12.A45");
        assertThat(cc.getLongitudePattern()).isEqualTo("E13 8.67B");
        assertThat(cc.getLatitudeString(x -> Value.of(3))).isEqualTo("N48°12.345'");
        assertThat(cc.getLongitudeString(x -> Value.of(3))).isEqualTo("E13°8.673'");
        final Geopoint gp = cc.calculateGeopoint(x -> Value.of(3));
        assertThat(gp.getLatitude()).isEqualTo(48.20575, offset(1e-8));
        assertThat(gp.getLongitude()).isEqualTo(13.14455, offset(1e-8));
    }

    @Test
    public void northSouthEastWest() {
        CalculatedCoordinate cc = CalculatedCoordinate.createFromConfig("{CC|N48.123|W13.456}");
        assertThat(cc.calculateGeopoint(null).getLatitude()).isEqualTo(48.123);
        assertThat(cc.calculateGeopoint(null).getLongitude()).isEqualTo(-13.456);

        cc = CalculatedCoordinate.createFromConfig("{CC|S48.123|E13.456}");
        assertThat(cc.calculateGeopoint(null).getLatitude()).isEqualTo(-48.123);
        assertThat(cc.calculateGeopoint(null).getLongitude()).isEqualTo(13.456);
    }
}