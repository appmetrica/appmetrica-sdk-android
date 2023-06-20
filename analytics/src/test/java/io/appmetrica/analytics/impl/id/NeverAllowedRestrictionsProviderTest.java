package io.appmetrica.analytics.impl.id;

import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class NeverAllowedRestrictionsProviderTest extends CommonTest {

    private final AdvertisingIdGetter.NeverAllowedRestrictionsProvider provider =
            new AdvertisingIdGetter.NeverAllowedRestrictionsProvider();

    @Test
    public void canTrackNullStartup() {
        assertThat(provider.canTrackAid(null)).isFalse();
    }

    @Test
    public void canTrackNonNullStartup() {
        assertThat(provider.canTrackAid(mock(StartupState.class))).isFalse();
    }
}
