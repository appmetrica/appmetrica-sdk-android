package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AllThreadsTest extends CommonTest {

    @Test
    public void testConstructor() {
        ThreadState mainThread = mock(ThreadState.class);
        List threads = Arrays.asList(mock(ThreadState.class), mock(ThreadState.class), mock(ThreadState.class));
        String processName = "processName";
        AllThreads dump = new AllThreads(mainThread, threads, processName);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dump.affectedThread).as("affectedThread").isSameAs(mainThread);
        assertions.assertThat(dump.threads).as("threads").containsExactlyElementsOf(threads);
        assertions.assertThat(dump.threads).as("threads").isNotSameAs(threads);
        assertions.assertThat(dump.processName).as("processName").isEqualTo(processName);
        assertions.assertAll();
    }

    @Test
    public void additionalConstructor() {
        String processName = "processName";
        AllThreads dump = new AllThreads(processName);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dump.affectedThread).as("affectedThread").isNull();
        assertions.assertThat(dump.threads).as("threads").isEmpty();
        assertions.assertThat(dump.processName).as("processName").isEqualTo(processName);
        assertions.assertAll();
    }

    @Test
    public void testNullListInConstructor() {
        ThreadState mainThread = mock(ThreadState.class);
        AllThreads dump = new AllThreads(mainThread, null, null);

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(dump.threads).as("threads").isNotNull();
        assertions.assertThat(dump.threads).as("threads").isEmpty();
        assertions.assertAll();
    }

}
