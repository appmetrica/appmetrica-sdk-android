package io.appmetrica.analytics.impl;

import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@Config(sdk = Build.VERSION_CODES.O)
@RunWith(RobolectricTestRunner.class)
public class BackgroundRestrictionsStateProviderPrePTest extends CommonTest {

    private BackgroundRestrictionsStateProvider mProvider;

    @Before
    public void setUp() {
        mProvider = new BackgroundRestrictionsStateProvider(RuntimeEnvironment.getApplication());
    }

    @Test
    public void testGetState() {
        assertThat(mProvider.getBackgroundRestrictionsState()).isNull();
    }
}
