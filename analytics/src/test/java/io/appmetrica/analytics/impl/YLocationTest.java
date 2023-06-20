package io.appmetrica.analytics.impl;

import android.location.Location;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class YLocationTest extends CommonTest {

    private final String mOriginalProvdier = "original_provider";

    @Test
    public void testOriginalProvider() {
        final Location originalLocation = new Location(mOriginalProvdier);
        final YLocation location = YLocation.createWithOriginalProvider(originalLocation);
        assertThat(location).isEqualToIgnoringGivenFields(originalLocation, "mProvider", "mOriginalProvider");
        assertThat(location.getOriginalProvider()).isEqualTo(mOriginalProvdier);
        assertThat(location.getProvider()).isEmpty();
    }

    @Test
    public void testOriginalLocationIsNotRewritten() {
        final Location originalLocation = new Location(mOriginalProvdier);
        YLocation.createWithOriginalProvider(originalLocation);
        assertThat(originalLocation.getProvider()).isEqualTo(mOriginalProvdier);
    }

    @Test
    public void testWithoutOriginalProvider() {
        final Location originalLocation = new Location(mOriginalProvdier);
        final YLocation location = YLocation.createWithoutOriginalProvider(originalLocation);
        assertThat(location).isEqualToIgnoringGivenFields(originalLocation, "mOriginalProvider");
        assertThat(location.getOriginalProvider()).isEmpty();
    }
}
