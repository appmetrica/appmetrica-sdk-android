package io.appmetrica.analytics.impl.billing;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class BillingInfoConverterTest extends CommonTest {

    private BillingInfoConverter converter;

    @Before
    public void setUp() {
        converter = new BillingInfoConverter();
    }

    @Test
    public void testToProtoIfInapp() throws Exception {
        BillingInfo model = new BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 41, 42);
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo proto = converter.fromModel(model);
        new ProtoObjectPropertyAssertions<AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo>(proto)
                .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE)
                .checkField("sku", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToProtoIfSubs() throws Exception {
        BillingInfo model = new BillingInfo(ProductType.SUBS, "sku", "purchaseToken", 41, 42);
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo proto = converter.fromModel(model);
        new ProtoObjectPropertyAssertions<AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo>(proto)
                .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION)
                .checkField("sku", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToProtoIfUnknown() throws Exception {
        BillingInfo model = new BillingInfo(ProductType.UNKNOWN, "sku", "purchaseToken", 41, 42);
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo proto = converter.fromModel(model);
        new ProtoObjectPropertyAssertions<AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo>(proto)
                .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN)
                .checkField("sku", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToModelEmpty() throws Exception {
        ObjectPropertyAssertions(
                converter.toModel(new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo())
        )
                .withFinalFieldOnly(false)
                .checkField("type", ProductType.UNKNOWN)
                .checkField("productId", "")
                .checkField("purchaseToken", "")
                .checkField("purchaseTime", 0L)
                .checkField("sendTime", 0L)
                .checkAll();
    }

    @Test
    public void testToModelIfPurchase() throws Exception {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        nano.type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE;
        nano.sku = "sku";
        nano.purchaseToken = "purchaseToken";
        nano.purchaseTime = 41;
        nano.sendTime = 42;

        ObjectPropertyAssertions(converter.toModel(nano))
                .withFinalFieldOnly(false)
                .checkField("type", ProductType.INAPP)
                .checkField("productId", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToModelIfSubscription() throws Exception {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        nano.type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION;
        nano.sku = "sku";
        nano.purchaseToken = "purchaseToken";
        nano.purchaseTime = 41;
        nano.sendTime = 42;

        ObjectPropertyAssertions(converter.toModel(nano))
                .withFinalFieldOnly(false)
                .checkField("type", ProductType.SUBS)
                .checkField("productId", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToModelIfUnknown() throws Exception {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        nano.type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN;
        nano.sku = "sku";
        nano.purchaseToken = "purchaseToken";
        nano.purchaseTime = 41;
        nano.sendTime = 42;

        ObjectPropertyAssertions(converter.toModel(nano))
                .withFinalFieldOnly(false)
                .checkField("type", ProductType.UNKNOWN)
                .checkField("productId", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }

    @Test
    public void testToModelIfOtherType() throws Exception {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        nano.type = 42;
        nano.sku = "sku";
        nano.purchaseToken = "purchaseToken";
        nano.purchaseTime = 41;
        nano.sendTime = 42;

        ObjectPropertyAssertions(converter.toModel(nano))
                .withFinalFieldOnly(false)
                .checkField("type", ProductType.UNKNOWN)
                .checkField("productId", "sku")
                .checkField("purchaseToken", "purchaseToken")
                .checkField("purchaseTime", 41L)
                .checkField("sendTime", 42L)
                .checkAll();
    }
}
