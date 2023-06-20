package io.appmetrica.analytics.impl.permissions;

import android.Manifest;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class LocationFlagStrategyTest extends CommonTest {

    @NonNull
    private final String permission;
    private final boolean locationTracking;
    private final boolean forbidden;

    @ParameterizedRobolectricTestRunner.Parameters(name = "Permission {0} check is forbidden ({2}) when locationsTracking is {1}.")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Manifest.permission.ACCESS_COARSE_LOCATION, false, true},
                {Manifest.permission.ACCESS_FINE_LOCATION, false, true},
                {Manifest.permission.READ_PHONE_STATE, false, false},
                {Manifest.permission.ACCESS_COARSE_LOCATION, true, false},
                {Manifest.permission.ACCESS_FINE_LOCATION, true, false},
                {Manifest.permission.READ_PHONE_STATE, true, false}
        });
    }

    public LocationFlagStrategyTest(@NonNull String permission, boolean locationTracking, boolean forbidden) {
        this.permission = permission;
        this.locationTracking = locationTracking;
        this.forbidden = forbidden;
    }

    private LocationFlagStrategy locationFlagStrategy;

    @Before
    public void setUp() {
        locationFlagStrategy = new LocationFlagStrategy();
    }

    @Test
    public void shouldAsk() {
        if (locationTracking) {
            locationFlagStrategy.startLocationTracking();
        }
        assertThat(locationFlagStrategy.forbidUsePermission(permission)).isEqualTo(forbidden);
    }

    @Test
    public void shouldAskAfterRefreshingStateAfterStartedLocationTracking() {
        locationFlagStrategy.startLocationTracking();
        if (locationTracking) {
            locationFlagStrategy.startLocationTracking();
        } else {
            locationFlagStrategy.stopLocationTracking();
        }
        assertThat(locationFlagStrategy.forbidUsePermission(permission)).isEqualTo(forbidden);
    }

    @Test
    public void shouldAskAfterRefreshingStateAfterStoppedLocationTracking() {
        locationFlagStrategy.stopLocationTracking();
        if (locationTracking) {
            locationFlagStrategy.startLocationTracking();
        } else {
            locationFlagStrategy.stopLocationTracking();
        }
        assertThat(locationFlagStrategy.forbidUsePermission(permission)).isEqualTo(forbidden);
    }
}
