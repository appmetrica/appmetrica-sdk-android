package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class EventStartTest extends CommonTest {

    @Test
    public void testConstructor() {
        final String buildId = "876554321";
        EventStart eventStart = new EventStart(buildId);

        ObjectPropertyAssertions<EventStart> assertions =
            ObjectPropertyAssertions(eventStart);
        assertions.checkField("buildId", buildId);
        assertions.checkAll();
    }

    @Test
    public void testConstructorWithNullable() {
        EventStart eventStart = new EventStart(null);

        ObjectPropertyAssertions<EventStart> assertions =
            ObjectPropertyAssertions(eventStart);
        assertions.checkField("buildId", (String) null);
        assertions.checkAll();
    }
}
