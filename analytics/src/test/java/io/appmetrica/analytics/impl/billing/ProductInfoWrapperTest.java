package io.appmetrica.analytics.impl.billing;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.impl.protobuf.backend.Revenue;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ProductInfoWrapperTest extends CommonTest {

    @Test
    public void testCorrect() throws InvalidProtocolBufferNanoException, IllegalAccessException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "EUR", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        new ProtoObjectPropertyAssertions<Revenue>(proto)
            .checkField("quantity", 2)
            .checkField("priceMicros", 2L)
            .checkField("currency", "EUR".getBytes())
            .checkField("productId", "sku2".getBytes())
            .checkFieldRecursively("receipt",
                new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                        try {
                            assertions
                                .checkField("data", "originalJson".getBytes())
                                .checkField("signature", "signature2".getBytes())
                                .checkAll();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
            .checkField("autoCollected", true)
            .checkField("guessedBuyerDevice", Revenue.THIS)
            .checkField("inAppType", Revenue.SUBSCRIPTION)
            .checkFieldRecursively("transactionInfo",
                new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                        try {
                            assertions
                                .checkField("id", "token2".getBytes())
                                .checkField("time", 1214324L)
                                .checkField("secondaryId", "".getBytes())
                                .checkField("secondaryTime", 0L)
                                .checkField("state", 0)
                                .checkAll();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
            .checkFieldRecursively("subscriptionInfo",
                new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                    @Override
                    public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                        try {
                            assertions
                                .checkField("autoRenewing", false)
                                .checkFieldRecursively("subscriptionPeriod",
                                    new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                                        @Override
                                        public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                                            try {
                                                assertions
                                                    .checkField("number", 1)
                                                    .checkField("timeUnit", Revenue.SubscriptionInfo.Period.MONTH)
                                                    .checkAll();
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    })
                                .checkFieldRecursively("introductoryInfo",
                                    new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                                        @Override
                                        public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                                            try {
                                                assertions
                                                    .checkField("priceMicros", 4L)
                                                    .checkFieldRecursively("period",
                                                        new Consumer<ObjectPropertyAssertions<Revenue.Receipt>>() {
                                                            @Override
                                                            public void accept(ObjectPropertyAssertions<Revenue.Receipt> assertions) {
                                                                try {
                                                                    assertions
                                                                        .checkField("number", 1)
                                                                        .checkField("timeUnit", Revenue.SubscriptionInfo.Period.DAY)
                                                                        .checkAll();
                                                                } catch (Exception e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                            }
                                                        })
                                                    .checkField("numberOfPeriods", 1)
                                                    .checkField("id", "".getBytes())
                                                    .checkAll();
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    })
                                .checkAll();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
            .checkField("payload", "".getBytes())
            .checkAll();
    }

    @Test
    public void testPeriodNull() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "EUR", 4, null, 1, null, "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod).isNull();
        assertThat(proto.subscriptionInfo.introductoryInfo.period).isNull();
    }

    @Test
    public void testIncorrectCurrency() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.currency).isEqualTo("".getBytes());
    }

    @Test
    public void testInappType() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.INAPP, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.inAppType).isEqualTo(Revenue.PURCHASE);
    }

    @Test
    public void testSubsType() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.inAppType).isEqualTo(Revenue.SUBSCRIPTION);
    }

    @Test
    public void testUnknownType() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.UNKNOWN, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.inAppType).isEqualTo(Revenue.PURCHASE);
    }

    @Test
    public void testTimeUnitDay() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.DAY), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.DAY);
    }

    @Test
    public void testTimeUnitWeek() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.WEEK), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.WEEK);
    }

    @Test
    public void testTimeUnitMonth() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.MONTH);
    }

    @Test
    public void testTimeUnitYear() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.YEAR), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.YEAR);
    }

    @Test
    public void testTimeUnitUnknown() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.TIME_UNIT_UNKNOWN), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.TIME_UNIT_UNKNOWN);
    }

    @Test
    public void testTypeIsNotSubs() throws InvalidProtocolBufferNanoException {
        final ProductInfo productInfo = new ProductInfo(ProductType.INAPP, "sku2", 2, 2, "test", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.TIME_UNIT_UNKNOWN), "signature2", "token2", 1214324123, false, "originalJson");
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        final byte[] data = wrapper.getDataToSend();

        Revenue proto = Revenue.parseFrom(data);

        assertThat(proto.subscriptionInfo).isNull();
    }
}
