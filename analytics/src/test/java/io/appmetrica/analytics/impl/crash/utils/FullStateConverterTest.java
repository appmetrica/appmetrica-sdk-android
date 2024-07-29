package io.appmetrica.analytics.impl.crash.utils;

import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class FullStateConverterTest extends CommonTest {

    private FullStateConverter converter = new FullStateConverter();
    private Thread thread = spy(new Thread("name"));

    @Test
    public void testConvert() {

        String name = "threadName";
        int priority = Thread.NORM_PRIORITY;
        long id = 1221;

        Thread thread = spy(new Thread(name));
        thread.setPriority(priority);
        doReturn(id).when(thread).getId();

        ThreadState state = converter.apply(thread, new StackTraceElement[]{new StackTraceElement(
                "s", "a", "a", 1221
        )});

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(state.name).as("name").isEqualTo(name);
        soft.assertThat(state.priority).as("priority").isEqualTo(priority);
        soft.assertThat(state.tid).as("id").isEqualTo(id);
        soft.assertThat(state.group).as("group").isEqualTo(thread.getThreadGroup().getName());
        soft.assertThat(state.stacktrace).as("stacktrace")
                .extracting("className", "methodName", "fileName", "lineNumber").containsExactly(
                tuple("s", "a", "a", 1221)
        );

        soft.assertAll();
    }

    @Test
    public void testThreadGroupWithName() {
        assertThat(FullStateConverter.getThreadGroupName(thread)).startsWith("SDK");
    }

}
