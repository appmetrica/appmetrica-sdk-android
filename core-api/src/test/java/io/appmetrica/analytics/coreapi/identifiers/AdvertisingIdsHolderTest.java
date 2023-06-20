package io.appmetrica.analytics.coreapi.identifiers;

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AdvertisingIdsHolderTest extends CommonTest {

    @Test
    public void testDefaultValues() throws IllegalAccessException {
        AdvertisingIdsHolder advertisingIdsHolder = new AdvertisingIdsHolder();
        ObjectPropertyAssertions(advertisingIdsHolder)
                .withPrivateFields(true)
                .checkFieldComparingFieldByFieldRecursively("mGoogle", new AdTrackingInfoResult())
                .checkFieldComparingFieldByFieldRecursively("mHuawei", new AdTrackingInfoResult())
                .checkFieldComparingFieldByFieldRecursively("yandex", new AdTrackingInfoResult())
                .checkAll();
    }

    @Test
    public void testSetIds() throws Exception {
        AdTrackingInfoResult google = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "google id", false),
                IdentifierStatus.OK,
                "error1"
        );
        AdTrackingInfoResult huawei = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.HMS, "huawei id", false),
                IdentifierStatus.OK,
                "error2"
        );
        AdTrackingInfoResult yandex = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, "yandex id", false),
                IdentifierStatus.OK,
                "error3"
        );
        AdvertisingIdsHolder advertisingIdsHolder = new AdvertisingIdsHolder(google, huawei, yandex);
        ObjectPropertyAssertions(advertisingIdsHolder)
                .withPrivateFields(true)
                .checkField("mGoogle", "getGoogle", google)
                .checkField("mHuawei", "getHuawei", huawei)
                .checkField("yandex", "getYandex", yandex)
                .checkAll();
    }
}
