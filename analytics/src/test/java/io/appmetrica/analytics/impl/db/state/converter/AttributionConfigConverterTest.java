package io.appmetrica.analytics.impl.db.state.converter;

import android.util.Pair;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AttributionConfigConverterTest extends CommonTest {

    private final AttributionConfigConverter converter = new AttributionConfigConverter();

    @Test
    public void toProtoEmptyConditions() throws IllegalAccessException {
        AttributionConfig model = new AttributionConfig(new ArrayList<Pair<String, AttributionConfig.Filter>>());
        new ProtoObjectPropertyAssertions<StartupStateProtobuf.StartupState.Attribution>(converter.fromModel(model))
            .checkField("deeplinkConditions", new StartupStateProtobuf.StartupState.Attribution.StringPair[0])
            .checkAll();
    }

    @Test
    public void toProtoSingleCondition() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution.StringPair nanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        nanoPair.key = "some key";
        nanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        nanoPair.filter.value = "some value";
        AttributionConfig model = new AttributionConfig(Collections.singletonList(new Pair<String, AttributionConfig.Filter>("some key", new AttributionConfig.Filter("some value"))));
        new ProtoObjectPropertyAssertions<StartupStateProtobuf.StartupState.Attribution>(converter.fromModel(model))
            .checkFieldComparingFieldByFieldRecursively("deeplinkConditions", new StartupStateProtobuf.StartupState.Attribution.StringPair[]{nanoPair})
            .checkAll();
    }

    @Test
    public void toProtoMultipleConditions() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution.StringPair firstNanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        firstNanoPair.key = "some key 1";
        firstNanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        firstNanoPair.filter.value = "some value 1";
        StartupStateProtobuf.StartupState.Attribution.StringPair secondNanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        secondNanoPair.key = "some key 2";
        secondNanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        secondNanoPair.filter.value = "some value 2";
        AttributionConfig model = new AttributionConfig(Arrays.asList(
            new Pair<String, AttributionConfig.Filter>("some key 1", new AttributionConfig.Filter("some value 1")),
            new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
        ));
        new ProtoObjectPropertyAssertions<StartupStateProtobuf.StartupState.Attribution>(converter.fromModel(model))
            .checkFieldComparingFieldByFieldRecursively("deeplinkConditions",
                new StartupStateProtobuf.StartupState.Attribution.StringPair[]{firstNanoPair, secondNanoPair})
            .checkAll();
    }

    @Test
    public void toProtoHasNullValue() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution.StringPair nanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        nanoPair.key = "some key";
        nanoPair.filter = null;
        AttributionConfig model = new AttributionConfig(Collections.singletonList(new Pair<String, AttributionConfig.Filter>("some key", null)));
        new ProtoObjectPropertyAssertions<StartupStateProtobuf.StartupState.Attribution>(converter.fromModel(model))
            .checkFieldComparingFieldByFieldRecursively("deeplinkConditions", new StartupStateProtobuf.StartupState.Attribution.StringPair[]{nanoPair})
            .checkAll();
    }

    @Test
    public void toModelEmptyConditions() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution proto = new StartupStateProtobuf.StartupState.Attribution();
        ObjectPropertyAssertions(converter.toModel(proto))
            .checkField("deeplinkConditions", new ArrayList<Pair<String, AttributionConfig.Filter>>())
            .checkAll();
    }

    @Test
    public void toModelSingleCondition() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution proto = new StartupStateProtobuf.StartupState.Attribution();
        StartupStateProtobuf.StartupState.Attribution.StringPair nanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        nanoPair.key = "some key 1";
        nanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        nanoPair.filter.value = "some value 1";
        proto.deeplinkConditions = new StartupStateProtobuf.StartupState.Attribution.StringPair[]{nanoPair};
        ObjectPropertyAssertions(converter.toModel(proto))
            .checkField("deeplinkConditions", Collections.singletonList(
                new Pair<String, AttributionConfig.Filter>("some key 1", new AttributionConfig.Filter("some value 1"))
            ), true)
            .checkAll();
    }

    @Test
    public void toModelMultipleConditions() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution proto = new StartupStateProtobuf.StartupState.Attribution();
        StartupStateProtobuf.StartupState.Attribution.StringPair firstNanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        firstNanoPair.key = "some key 1";
        firstNanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        firstNanoPair.filter.value = "some value 1";
        StartupStateProtobuf.StartupState.Attribution.StringPair secondNanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        secondNanoPair.key = "some key 2";
        secondNanoPair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
        secondNanoPair.filter.value = "some value 2";
        proto.deeplinkConditions = new StartupStateProtobuf.StartupState.Attribution.StringPair[]{firstNanoPair, secondNanoPair};
        ObjectPropertyAssertions(converter.toModel(proto))
            .checkField("deeplinkConditions", Arrays.asList(
                new Pair<String, AttributionConfig.Filter>("some key 1", new AttributionConfig.Filter("some value 1")),
                new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
            ), true)
            .checkAll();
    }

    @Test
    public void toModelHasNullValue() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.Attribution proto = new StartupStateProtobuf.StartupState.Attribution();
        StartupStateProtobuf.StartupState.Attribution.StringPair nanoPair = new StartupStateProtobuf.StartupState.Attribution.StringPair();
        nanoPair.key = "some key 1";
        nanoPair.filter = null;
        proto.deeplinkConditions = new StartupStateProtobuf.StartupState.Attribution.StringPair[]{nanoPair};
        ObjectPropertyAssertions(converter.toModel(proto))
            .checkField("deeplinkConditions", Collections.singletonList(new Pair<String, AttributionConfig.Filter>("some key 1", null)))
            .checkAll();
    }
}
