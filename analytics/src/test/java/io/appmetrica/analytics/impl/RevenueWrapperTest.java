package io.appmetrica.analytics.impl;

import android.util.Pair;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Currency;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RevenueWrapperTest extends CommonTest {

    private static final String TRUNCATED_DATA_MESSAGE = "<truncated data was not sent, exceeded the limit of 180kb>";

    @Mock
    private PublicLogger mPublicLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testWithRequiredMicros() throws InvalidProtocolBufferNanoException {
        Revenue revenue = Revenue.newBuilder(55500000, Currency.getInstance("USD")).build();
        Pair<byte[], Integer> data = new RevenueWrapper(revenue, mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(data.first);

        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(proto.currency).as("currency").isEqualTo("USD".getBytes());
        softAssertion.assertThat(proto.priceMicros).as("priceMicros").isEqualTo(55500000);

        softAssertion.assertThat(data.second).as("bytes_truncated").isZero();
        softAssertion.assertAll();
    }

    @Test
    public void testWithAll() throws InvalidProtocolBufferNanoException {
        Pair<byte[], Integer> data = new RevenueWrapper(Revenue.newBuilder(100, Currency.getInstance("USD"))
            .withPayload("payload")
            .withProductID("productID")
            .withQuantity(300)
            .withReceipt(Revenue.Receipt.newBuilder()
                .withSignature("signature")
                .withData("data")
                .build())
            .build(), mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(data.first);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(proto.payload).as("payload").isEqualTo("payload".getBytes());
        assertion.assertThat(proto.productId).as("productID").isEqualTo("productID".getBytes());
        assertion.assertThat(proto.quantity).as("quantity").isEqualTo(300);
        assertion.assertThat(proto.receipt.data).as("receipt.data").isEqualTo("data".getBytes());
        assertion.assertThat(proto.receipt.signature).as("receipt.signature").isEqualTo("signature".getBytes());
        assertion.assertAll();

        assertThat(data.second).isZero();
    }

    @Test
    public void testWithHuge() throws InvalidProtocolBufferNanoException {
        String data = new RandomStringGenerator(EventLimitationProcessor.RECEIPT_DATA_MAX_SIZE + 1).nextString();
        String signature = new RandomStringGenerator(EventLimitationProcessor.RECEIPT_SIGNATURE_MAX_LENGTH + 1).nextString();
        Pair<byte[], Integer> pair = new RevenueWrapper(Revenue.newBuilder(100, Currency.getInstance("USD"))
            .withReceipt(Revenue.Receipt.newBuilder()
                .withSignature(signature)
                .withData(data)
                .build())
            .build(), mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(pair.first);
        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(proto.receipt.data)
            .as("receipt.data")
            .isEqualTo(TRUNCATED_DATA_MESSAGE.getBytes());
        assertion.assertThat(proto.receipt.signature)
            .as("receipt.signature")
            .isEqualTo(TRUNCATED_DATA_MESSAGE.getBytes());
        assertion.assertAll();

        assertThat(pair.second).isEqualTo(data.getBytes().length);
    }

    @Test
    public void testWithHugePayload() throws InvalidProtocolBufferNanoException {
        String payload = new RandomStringGenerator(EventLimitationProcessor.REVENUE_PAYLOAD_MAX_SIZE + 1).nextString();
        Pair<byte[], Integer> data = new RevenueWrapper(Revenue.newBuilder(100, Currency.getInstance("USD"))
            .withPayload(payload)
            .build(), mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(data.first);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(proto.payload).as("payload")
            .isEqualTo(new StringByBytesTrimmer(
                EventLimitationProcessor.REVENUE_PAYLOAD_MAX_SIZE, "test payload", mPublicLogger
            ).trim(payload).getBytes());
        assertion.assertThat(data.second).as("bytesTruncated").isZero();
        assertion.assertAll();
    }

    @Test
    public void testWithHugeData() throws InvalidProtocolBufferNanoException {
        String string = new RandomStringGenerator(EventLimitationProcessor.RECEIPT_DATA_MAX_SIZE + 1).nextString();
        Pair<byte[], Integer> data = new RevenueWrapper(Revenue.newBuilder(100, Currency.getInstance("USD"))
            .withReceipt(Revenue.Receipt.newBuilder()
                .withData(string)
                .build())
            .build(), mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(data.first);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(proto.receipt.data).as("receipt.data")
            .isEqualTo(TRUNCATED_DATA_MESSAGE.getBytes());
        assertion.assertThat(data.second).as("bytesTruncated").isEqualTo(string.getBytes().length);
        assertion.assertAll();
    }

    @Test
    public void testWithHugeSignature() throws InvalidProtocolBufferNanoException {
        String string = new RandomStringGenerator(EventLimitationProcessor.RECEIPT_SIGNATURE_MAX_LENGTH + 1).nextString();
        Pair<byte[], Integer> data = new RevenueWrapper(Revenue.newBuilder(100, Currency.getInstance("USD"))
            .withReceipt(Revenue.Receipt.newBuilder()
                .withSignature(string)
                .build())
            .build(), mPublicLogger).getDataToSend();

        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto = io.appmetrica.analytics.impl.protobuf.backend.Revenue.parseFrom(data.first);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(proto.receipt.signature)
            .as("receipt.signature")
            .isEqualTo(TRUNCATED_DATA_MESSAGE.getBytes());
        assertion.assertThat(data.second).as("bytesTruncated").isZero();
        assertion.assertAll();
    }

}
