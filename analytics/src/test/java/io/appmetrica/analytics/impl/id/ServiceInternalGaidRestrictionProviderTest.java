package io.appmetrica.analytics.impl.id;

import androidx.annotation.NonNull;
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
public class ServiceInternalGaidRestrictionProviderTest extends CommonTest {

    @NonNull
    private final StartupState mState;
    @NonNull
    private final boolean mShouldCollect;

    private final AdvertisingIdGetter.ServiceInternalGaidRestrictionProvider mProvider = new AdvertisingIdGetter.ServiceInternalGaidRestrictionProvider();

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, false},
                {new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().withGoogleAid(false).build()).withHadFirstStartup(true).build(), false},
                {new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).withHadFirstStartup(true).build(), false},
                {new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().withGoogleAid(true).build()).build(), true},
                {new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).withHadFirstStartup(false).build(), true},
        });
    }

    public ServiceInternalGaidRestrictionProviderTest(@NonNull StartupState state, @NonNull boolean shouldCollect) {
        mState = state;
        mShouldCollect = shouldCollect;
    }

    @Test
    public void test() {
        assertThat(mProvider.canTrackAid(mState)).isEqualTo(mShouldCollect);
    }
}
