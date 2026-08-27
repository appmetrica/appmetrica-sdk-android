package io.appmetrica.analytics.impl;

import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

public class EventStartTest extends CommonTest {

    @Test
    public void testConstructor() {
        final String buildId = "876554321";
        EventStart eventStart = new EventStart(buildId);

        ObjectPropertyAssertions<EventStart> assertions =
            Assertions.INSTANCE.ObjectPropertyAssertions(eventStart);
        assertions.checkField("buildId", buildId);
        assertions.checkAll();
    }

    @Test
    public void testConstructorWithNullable() {
        EventStart eventStart = new EventStart(null);

        ObjectPropertyAssertions<EventStart> assertions =
            Assertions.INSTANCE.ObjectPropertyAssertions(eventStart);
        assertions.checkField("buildId", (String) null);
        assertions.checkAll();
    }
}
