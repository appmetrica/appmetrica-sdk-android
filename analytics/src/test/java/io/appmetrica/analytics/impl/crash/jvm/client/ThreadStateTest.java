package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class ThreadStateTest extends CommonTest {

    public static ThreadState createEmpty() {
        return new ThreadState(
            "name", 1, 1, "group", 1, Collections.emptyList()
        );
    }

    @Test
    public void testNullStacktrace() {
        assertThat(
            new ThreadState("name", 1, 1, "group", 1, null).stacktrace
        ).isNotNull().isEmpty();
    }

    @Test
    public void testProperties() throws IllegalAccessException {
        String name = "name";
        int priority = 250;
        long tid = 2;
        String group = "group";
        int state = 1;
        List<StackTraceElement> stacktrace = Collections.emptyList();
        ThreadState threadState = new ThreadState(name, priority, tid, group, state, stacktrace);

        ObjectPropertyAssertions<ThreadState> assertions = ObjectPropertyAssertions(threadState);

        assertions.checkField("stacktrace", stacktrace)
            .checkField("name", name)
            .checkField("priority", priority)
            .checkField("tid", tid)
            .checkField("group", group)
            .checkField("state", state)
            .checkAll();
    }
}
