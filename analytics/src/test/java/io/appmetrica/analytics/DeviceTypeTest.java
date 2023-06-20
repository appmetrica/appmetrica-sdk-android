package io.appmetrica.analytics;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DeviceTypeTest extends CommonTest {

    private static final String[] mPersistentValues;

    static {
        mPersistentValues = new String[DeviceType.values().length];
        for (int i = 0; i < DeviceType.values().length; i++) {
            mPersistentValues[i] = DeviceType.values()[i].getType();
        }
    }

    @Test
    public void testRestoringFromValidValue() {
        for (String value : mPersistentValues) {
            DeviceType deviceType = DeviceType.of(value);
            assertThat(deviceType.getType()).isEqualTo(value);
        }
    }

    @Test
    public void testRestoringFromInvalidValue() {
        assertThat(DeviceType.of("")).isNull();
        assertThat(DeviceType.of(null)).isNull();
        assertThat(DeviceType.of("invalid")).isNull();
    }
}
