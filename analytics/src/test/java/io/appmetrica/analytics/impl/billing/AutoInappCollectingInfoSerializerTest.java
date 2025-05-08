package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AutoInappCollectingInfoSerializerTest extends CommonTest {

    private final AutoInappCollectingInfoSerializer mSerializer = new AutoInappCollectingInfoSerializer();

    @Test
    public void testToByteArrayDefaultObject() throws IOException {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo protoState =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo();
        byte[] rawData = mSerializer.toByteArray(protoState);
        AutoInappCollectingInfoProto.AutoInappCollectingInfo restored = mSerializer.toState(rawData);
        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState);
    }

    @Test
    public void testToByteArrayFilledObject() throws IOException {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo protoState =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo();
        protoState.entries = new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo[]{
            createProtoBillingInfo(AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE, "sku", "purchaseToken", 41, 42)
        };
        protoState.firstInappCheckOccurred = false;

        byte[] rawData = mSerializer.toByteArray(protoState);
        assertThat(rawData).isNotEmpty();
        AutoInappCollectingInfoProto.AutoInappCollectingInfo restored = mSerializer.toState(rawData);
        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState);
    }

    @Test(expected = InvalidProtocolBufferNanoException.class)
    public void testDeserializationInvalidByteArray() throws IOException {
        mSerializer.toState(new byte[]{1, 2, 3});
    }

    @Test
    public void testDefaultValue() {
        assertThat(mSerializer.defaultValue()).usingRecursiveComparison().isEqualTo(
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo()
        );
    }

    private AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo createProtoBillingInfo(
        final int type,
        @NonNull final String sku,
        @NonNull final String purchaseToken,
        final long purchaseTime,
        final long sendTime
    ) {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo proto =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        proto.type = type;
        proto.sku = sku;
        proto.purchaseToken = purchaseToken;
        proto.purchaseTime = purchaseTime;
        proto.sendTime = sendTime;
        return proto;
    }
}
