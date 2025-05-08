package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AutoInappCollectingConfigParserTest extends CommonTest {

    private final AutoInappCollectingConfigParser parser = new AutoInappCollectingConfigParser();
    private final StartupResult result = new StartupResult();

    @Test
    public void testNoBlock() throws Exception {
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        parser.parse(result, startupJsonMock);

        ObjectPropertyAssertions<BillingConfig> assertions =
            ObjectPropertyAssertions(result.getAutoInappCollectingConfig());

        assertions.checkField("sendFrequencySeconds", 86400);
        assertions.checkField("firstCollectingInappMaxAgeSeconds", 86400);
        assertions.checkAll();
    }

    @Test
    public void testHasBlockNoData() throws Exception {
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        startupJsonMock.addEmptyAutoInappCollectingConfig();
        parser.parse(result, startupJsonMock);

        ObjectPropertyAssertions<BillingConfig> assertions =
            ObjectPropertyAssertions(result.getAutoInappCollectingConfig());

        assertions.checkField("sendFrequencySeconds", 86400);
        assertions.checkField("firstCollectingInappMaxAgeSeconds", 86400);
        assertions.checkAll();
    }

    @Test
    public void testHasData() throws Exception {
        final int sendFrequencySeconds = 1;
        final int firstCollectingInappMaxAgeSeconds = 2;
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        startupJsonMock.addAutoInappCollectingConfig(sendFrequencySeconds, firstCollectingInappMaxAgeSeconds);

        parser.parse(result, startupJsonMock);

        ObjectPropertyAssertions<BillingConfig> assertions =
            ObjectPropertyAssertions(result.getAutoInappCollectingConfig());

        assertions.checkField("sendFrequencySeconds", sendFrequencySeconds);
        assertions.checkField("firstCollectingInappMaxAgeSeconds", firstCollectingInappMaxAgeSeconds);
        assertions.checkAll();
    }
}
