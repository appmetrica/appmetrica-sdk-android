package io.appmetrica.analytics.impl.startup.executor;

import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

public class StubbedStartupExecutorTest extends CommonTest {

    @Test
    public void testStubbedExecutor() {
        new StubbedStartupExecutor().sendStartupIfRequired();
    }

}
