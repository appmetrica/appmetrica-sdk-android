package io.appmetrica.analytics.impl;

import io.appmetrica.gradle.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

public class EventStartConverterTest extends CommonTest {

    private final EventStartConverter mEventStartConverter = new EventStartConverter();

    @Test
    public void testToProto() {
        final String buildId = "22222222";
        EventStart model = new EventStart(buildId);
        io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value proto = mEventStartConverter.fromModel(model);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(proto.buildId).isEqualTo(buildId.getBytes());
        softly.assertAll();
    }

    @Test
    public void testToProtoDefault() {
        EventStart model = new EventStart(null);
        io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value proto = mEventStartConverter.fromModel(model);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(proto.buildId).isEmpty();
        softly.assertAll();
    }

    @Test
    public void testToModel() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String buildId = "22222222";
        io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value proto = new io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value();
        proto.buildId = buildId.getBytes();
        EventStart model = mEventStartConverter.toModel(proto);
        ObjectPropertyAssertions<EventStart> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(model);
        assertions.checkField("buildId", buildId);
        assertions.checkAll();
    }

    @Test
    public void testToModelDefault() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value proto = new io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value();
        EventStart model = mEventStartConverter.toModel(proto);
        ObjectPropertyAssertions<EventStart> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(model);
        assertions.checkField("buildId", "");
        assertions.checkAll();
    }
}
