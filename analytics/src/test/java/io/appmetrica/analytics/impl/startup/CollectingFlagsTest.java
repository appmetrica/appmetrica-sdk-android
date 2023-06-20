package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class CollectingFlagsTest extends CommonTest {

    @Test
    public void testCreateFromBuilder() throws Exception {

        final boolean permissionsCollectingEnabled = true;
        final boolean featuresCollectingEnabled = true;

        final boolean googleAid = true;
        final boolean huaweiOaid = true;

        final boolean simInfo = true;
        final boolean sslPinning = true;

        CollectingFlags flags = new CollectingFlags.CollectingFlagsBuilder()
                .withPermissionsCollectingEnabled(permissionsCollectingEnabled)
                .withFeaturesCollectingEnabled(featuresCollectingEnabled)
                .withGoogleAid(googleAid)
                .withSimInfo(simInfo)
                .withHuaweiOaid(huaweiOaid)
                .withSslPinning(true)
                .build();

        ObjectPropertyAssertions<CollectingFlags> assertions =
                ObjectPropertyAssertions(flags)
                        .withFinalFieldOnly(false);

        assertions.checkField("permissionsCollectingEnabled", permissionsCollectingEnabled);
        assertions.checkField("featuresCollectingEnabled", featuresCollectingEnabled);
        assertions.checkField("googleAid", googleAid);
        assertions.checkField("simInfo", simInfo);
        assertions.checkField("huaweiOaid", huaweiOaid);
        assertions.checkField("sslPinning", sslPinning);

        assertions.checkAll();
    }

    @Test
    public void testCreateFromEmptyBuilder() throws Exception {
        CollectingFlags flags = new CollectingFlags.CollectingFlagsBuilder().build();
        ObjectPropertyAssertions<CollectingFlags> assertions =
                ObjectPropertyAssertions(flags)
                        .withFinalFieldOnly(false);

        assertions.checkField("permissionsCollectingEnabled", false);
        assertions.checkField("featuresCollectingEnabled", false);
        assertions.checkField("googleAid", false);
        assertions.checkField("simInfo", false);
        assertions.checkField("huaweiOaid", false);
        assertions.checkFieldIsNull("sslPinning");

        assertions.checkAll();
    }
}
