package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainProcessDetectorTest extends CommonTest {

    private MainProcessDetector mMainProcessDetector;

    @Before
    public void setUp() throws Exception {
        mMainProcessDetector = new MainProcessDetector();
    }

    @Test
    public void testIsMainProcess() {
        assertThat(mMainProcessDetector.isMainProcess()).isTrue();
    }
}
