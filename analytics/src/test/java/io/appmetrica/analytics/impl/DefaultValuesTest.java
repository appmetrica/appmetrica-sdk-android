package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DefaultValuesTest extends CommonTest {

    @Test
    public void defaultAutoAppOpenEnabled() {
        assertThat(DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED).isTrue();
    }

    @Test
    public void startupUpdateConfig() throws IllegalAccessException {
        new ProtoObjectPropertyAssertions<>(DefaultValues.STARTUP_UPDATE_CONFIG)
                .checkField("interval", 86400)
                .checkAll();
    }
}
