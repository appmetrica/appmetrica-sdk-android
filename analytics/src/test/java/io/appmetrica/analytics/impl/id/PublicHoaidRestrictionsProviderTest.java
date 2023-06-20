package io.appmetrica.analytics.impl.id;

import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicHoaidRestrictionsProviderTest extends CommonTest {

    private final AdvertisingIdGetter.PublicHoaidRestrictionProvider provider =
            new AdvertisingIdGetter.PublicHoaidRestrictionProvider();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void nullStartup() {
        assertThat(provider.canTrackAid(null)).isFalse();
    }

    @Test
    public void noFeature() {
        StartupState startupState = TestUtils.createDefaultStartupState();
        assertThat(provider.canTrackAid(startupState)).isFalse();
    }

    @Test
    public void disabledFeature() {
        StartupState startupState = new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().withHuaweiOaid(false).build()
        ).build();
        assertThat(provider.canTrackAid(startupState)).isFalse();
    }

    @Test
    public void enabledFeature() {
        StartupState startupState = new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().withHuaweiOaid(true).build()
        ).build();
        assertThat(provider.canTrackAid(startupState)).isTrue();
    }

}
