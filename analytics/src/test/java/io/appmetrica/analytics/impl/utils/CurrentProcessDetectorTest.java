package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CurrentProcessDetectorTest extends CommonTest {

    private CurrentProcessDetector mCurrentProcessDetector;

    @Before
    public void setUp() throws Exception {
        mCurrentProcessDetector = new CurrentProcessDetector();
    }

    @Test
    public void testIsMainProcess() {
        assertThat(mCurrentProcessDetector.isMainProcess()).isTrue();
    }
}
