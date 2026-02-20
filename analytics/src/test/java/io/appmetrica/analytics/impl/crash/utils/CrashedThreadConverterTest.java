package io.appmetrica.analytics.impl.crash.utils;

import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class CrashedThreadConverterTest extends CommonTest {

    private final CrashedThreadConverter converter = new CrashedThreadConverter();

    @Test
    public void testConvert() {

        String name = "threadName";
        int priority = Thread.NORM_PRIORITY;
        long id = 1221;

        Thread thread = spy(new Thread(name));
        thread.setPriority(priority);
        doReturn(id).when(thread).getId();
        doReturn(new StackTraceElement[]{new StackTraceElement(
            "s", "a", "a", 1221
        )}).when(thread).getStackTrace();

        ThreadState state = converter.apply(thread);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(state.name).as("name").isEqualTo(name);
        soft.assertThat(state.priority).as("priority").isEqualTo(priority);
        soft.assertThat(state.tid).as("id").isEqualTo(id);
        soft.assertThat(state.group).as("group").isEqualTo(thread.getThreadGroup().getName());
        soft.assertThat(state.stacktrace).as("stacktrace").isEmpty();

        soft.assertAll();
    }

}
