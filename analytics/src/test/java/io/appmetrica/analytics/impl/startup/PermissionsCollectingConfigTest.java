package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionsCollectingConfigTest extends CommonTest {

    private final long mCheckIntervalSeconds = 555333;
    private final long mForceSendIntervalSeconds = 999000;
    private PermissionsCollectingConfig mConfig;

    @Before
    public void setUp() {
        mConfig = new PermissionsCollectingConfig(mCheckIntervalSeconds, mForceSendIntervalSeconds);
    }

    @Test
    public void testConstructor() {
        assertThat(mConfig.mCheckIntervalSeconds).isEqualTo(mCheckIntervalSeconds);
        assertThat(mConfig.mForceSendIntervalSeconds).isEqualTo(mForceSendIntervalSeconds);
    }
}
