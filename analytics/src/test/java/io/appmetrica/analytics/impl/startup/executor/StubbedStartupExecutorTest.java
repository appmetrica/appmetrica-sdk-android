package io.appmetrica.analytics.impl.startup.executor;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class StubbedStartupExecutorTest extends CommonTest {

    @Test
    public void testStubbedExecutor() {
        new StubbedStartupExecutor().sendStartupIfRequired();
    }

}
