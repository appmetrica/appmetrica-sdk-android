package io.appmetrica.analytics.impl.id;

import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ServicePublicGaidRestrictionProviderTest extends CommonTest {

    private final AdvertisingIdGetter.ServicePublicGaidRestrictionProvider provider =
            new AdvertisingIdGetter.ServicePublicGaidRestrictionProvider();

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
                new CollectingFlags.CollectingFlagsBuilder().withGoogleAid(false).build()
        ).build();
        assertThat(provider.canTrackAid(startupState)).isFalse();
    }

    @Test
    public void enabledFeature() {
        StartupState startupState = new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().withGoogleAid(true).build()
        ).build();
        assertThat(provider.canTrackAid(startupState)).isTrue();
    }
}
