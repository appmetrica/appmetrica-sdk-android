package io.appmetrica.analytics.impl.component.remarketing;

import io.appmetrica.analytics.impl.protobuf.client.Eventhashes;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventHashesConverterTest extends CommonTest {
    private final EventHashesConverter mConverter = new EventHashesConverter();

    @Test
    public void testDefaultToProto() {
        EventHashes eventHashes = new EventHashes();
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        assertThat(protoEventHashes).isEqualToComparingFieldByField(mConverter.fromModel(eventHashes));
    }

    @Test
    public void testFilledToProto() {
        EventHashes eventHashes = new EventHashes(true, 100, 200, new int[]{1, 2, 3});
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        protoEventHashes.treatUnknownEventAsNew = true;
        protoEventHashes.lastVersionCode = 100;
        protoEventHashes.hashesCountFromPreviousVersion = 200;
        protoEventHashes.eventNameHashes = new int[]{1, 2, 3};
        assertThat(protoEventHashes).isEqualToComparingFieldByField(mConverter.fromModel(eventHashes));
    }

    @Test
    public void testDefaultToModel() {
        EventHashes eventHashes = new EventHashes();
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        assertThat(eventHashes).isEqualToComparingFieldByField(mConverter.toModel(protoEventHashes));
    }

    @Test
    public void testFilledToModel() {
        EventHashes eventHashes = new EventHashes(true, 100, 200, new int[]{1, 2, 3});
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        protoEventHashes.treatUnknownEventAsNew = true;
        protoEventHashes.lastVersionCode = 100;
        protoEventHashes.hashesCountFromPreviousVersion = 200;
        protoEventHashes.eventNameHashes = new int[]{1, 2, 3};
        assertThat(eventHashes).isEqualToComparingFieldByField(mConverter.toModel(protoEventHashes));
    }
}
