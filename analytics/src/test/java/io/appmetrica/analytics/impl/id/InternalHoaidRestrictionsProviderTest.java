package io.appmetrica.analytics.impl.id;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class InternalHoaidRestrictionsProviderTest extends CommonTest {

    @Nullable
    private final StartupState startupState;
    private final boolean shouldCollect;

    private final AdvertisingIdGetter.InternalHoaidRestrictionProvider mProvider =
            new AdvertisingIdGetter.InternalHoaidRestrictionProvider();

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { null, false },
                {
                    new StartupState.Builder(
                            new CollectingFlags.CollectingFlagsBuilder()
                                    .withHuaweiOaid(false)
                                    .build()
                    )
                            .withHadFirstStartup(true)
                            .build(),
                    false
                },
                {
                    new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build())
                            .withHadFirstStartup(true)
                            .build(),
                    false
                },
                {
                    new StartupState.Builder(
                            new CollectingFlags.CollectingFlagsBuilder()
                                    .withHuaweiOaid(true)
                                    .build()
                    ).build(),
                    true
                },
                {
                    new StartupState.Builder(
                            new CollectingFlags.CollectingFlagsBuilder().build()
                    )
                            .withHadFirstStartup(false)
                            .build(),
                    true
                },
        });
    }

    public InternalHoaidRestrictionsProviderTest(@Nullable StartupState startupState, boolean shouldCollect) {
        this.startupState = startupState;
        this.shouldCollect = shouldCollect;
    }

    @Test
    public void canTrackAid() {
        assertThat(mProvider.canTrackAid(startupState)).isEqualTo(shouldCollect);
    }
}
