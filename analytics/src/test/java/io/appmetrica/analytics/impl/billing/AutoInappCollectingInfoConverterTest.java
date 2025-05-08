package io.appmetrica.analytics.impl.billing;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AutoInappCollectingInfoConverterTest extends CommonTest {

    private final AutoInappCollectingInfoConverter mConverter = new AutoInappCollectingInfoConverter();

    @Test
    public void testDefaultToProto() throws IllegalAccessException {
        AutoInappCollectingInfo autoInappCollectingInfo =
            new AutoInappCollectingInfo(new ArrayList<BillingInfo>(), false);

        new ProtoObjectPropertyAssertions<AutoInappCollectingInfoProto.AutoInappCollectingInfo>(mConverter.fromModel(autoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", false)
            .checkField("entries", new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo[0])
            .checkAll();
    }

    @Test
    public void testFilledToProto() throws IllegalAccessException {
        AutoInappCollectingInfo autoInappCollectingInfo =
            new AutoInappCollectingInfo(getBillingInfoList(), true);

        new ProtoObjectPropertyAssertions<AutoInappCollectingInfoProto.AutoInappCollectingInfo>(mConverter.fromModel(autoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", true)
            .checkFieldComparingFieldByFieldRecursively("entries", getBillingInfoListProto())
            .checkAll();
    }

    @Test
    public void testDefaultToModel() throws IllegalAccessException {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo protoAutoInappCollectingInfo =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo();

        ObjectPropertyAssertions(mConverter.toModel(protoAutoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", false)
            .checkField("billingInfos", new ArrayList<BillingInfo>())
            .checkAll();
    }

    @Test
    public void testFilledToModel() throws IllegalAccessException {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo protoAutoInappCollectingInfo =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo();
        protoAutoInappCollectingInfo.firstInappCheckOccurred = true;
        protoAutoInappCollectingInfo.entries = getBillingInfoListProto();

        ObjectPropertyAssertions(mConverter.toModel(protoAutoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", true)
            .checkFieldComparingFieldByFieldRecursively("billingInfos", getBillingInfoList())
            .checkAll();
    }

    private List<BillingInfo> getBillingInfoList() {
        List<BillingInfo> billingInfoList = new ArrayList<BillingInfo>();
        billingInfoList.add(new BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 41, 42));
        return billingInfoList;
    }

    private AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo[] getBillingInfoListProto() {
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo info =
            new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        info.type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE;
        info.sku = "sku";
        info.purchaseToken = "purchaseToken";
        info.purchaseTime = 41L;
        info.sendTime = 42L;
        return new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo[]{info};
    }
}
