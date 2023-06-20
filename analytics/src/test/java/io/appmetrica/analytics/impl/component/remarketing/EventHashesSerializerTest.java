package io.appmetrica.analytics.impl.component.remarketing;

import io.appmetrica.analytics.impl.protobuf.client.Eventhashes;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class EventHashesSerializerTest extends CommonTest {

    private EventHashesSerializer mEventHashesSerializer;

    @Before
    public void setUp() throws Exception {
        mEventHashesSerializer = new EventHashesSerializer();
    }

    @Test
    public void testToByteArrayDefaultObject() throws IOException {
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        byte[] rawData = mEventHashesSerializer.toByteArray(protoEventHashes);
        assertThat(rawData).isNotEmpty();
        Eventhashes.EventHashes restoredEventHashes = mEventHashesSerializer.toState(rawData);
        assertThat(restoredEventHashes).isEqualToComparingFieldByField(protoEventHashes);
    }

    @Test
    public void testToByteArrayFilledObject() throws IOException {
        Eventhashes.EventHashes protoEventHashes = new Eventhashes.EventHashes();
        protoEventHashes.treatUnknownEventAsNew = true;
        protoEventHashes.lastVersionCode = 100;
        protoEventHashes.hashesCountFromPreviousVersion = 200;
        protoEventHashes.eventNameHashes = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000};
        byte[] rawData = mEventHashesSerializer.toByteArray(protoEventHashes);
        assertThat(rawData).isNotEmpty();
        Eventhashes.EventHashes restoredEventHashes = mEventHashesSerializer.toState(rawData);
        assertThat(restoredEventHashes).isEqualToComparingFieldByField(protoEventHashes);
    }

    @Test(expected = InvalidProtocolBufferNanoException.class)
    public void testDeserializationInvalidByteArray() throws IOException {
        mEventHashesSerializer.toState(new byte[]{1, 2, 3});
    }
}
