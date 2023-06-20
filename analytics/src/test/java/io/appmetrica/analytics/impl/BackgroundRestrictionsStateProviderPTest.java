package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Build;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@Config(sdk = Build.VERSION_CODES.P)
@RunWith(RobolectricTestRunner.class)
public class BackgroundRestrictionsStateProviderPTest extends CommonTest {

    private Context context;
    private BackgroundRestrictionsStateProvider provider;
    private BackgroundRestrictionsState.AppStandByBucket appStandByBucket =
            BackgroundRestrictionsState.AppStandByBucket.FREQUENT;
    @Mock
    private AppStandbyBucketConverter appStandbyBucketConverter;
    @Mock
    private BackgroundRestrictionsState backgroundRestrictionsState;
    @Rule
    public MockedStaticRule<BackgroundRestrictionStateProviderHelperForP> sHelper =
            new MockedStaticRule<>(BackgroundRestrictionStateProviderHelperForP.class);
    @Rule
    public MockedStaticRule<AndroidUtils> sUtils = new MockedStaticRule<>(AndroidUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(appStandbyBucketConverter.fromIntToAppStandbyBucket(anyInt())).thenReturn(appStandByBucket);
        provider = new BackgroundRestrictionsStateProvider(context, appStandbyBucketConverter);
        when(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(
                context, appStandbyBucketConverter))
                .thenReturn(backgroundRestrictionsState);
        when(AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)).thenReturn(true);
    }

    @Test
    public void getBackgroundRestrictionsState() {
        assertThat(provider.getBackgroundRestrictionsState()).isEqualTo(backgroundRestrictionsState);
    }
}
