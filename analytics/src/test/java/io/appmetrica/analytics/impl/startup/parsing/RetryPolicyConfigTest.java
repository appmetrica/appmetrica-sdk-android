package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class RetryPolicyConfigTest extends CommonTest {

    private final RetryPolicyConfigParser mParser = new RetryPolicyConfigParser();
    private final StartupResult mResult = new StartupResult();

    @Test
    public void testNoBlock() throws Exception {
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        mParser.parse(mResult, startupJsonMock);

        ObjectPropertyAssertions<RetryPolicyConfig> assertions =
            ObjectPropertyAssertions(mResult.getRetryPolicyConfig())
                .withFinalFieldOnly(false);

        assertions.checkField("maxIntervalSeconds", 600);
        assertions.checkField("exponentialMultiplier", 1);
        assertions.checkAll();
    }

    @Test
    public void testHasBlockNoData() throws Exception {
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        startupJsonMock.addEmptyRetryPolicyConfig();
        mParser.parse(mResult, startupJsonMock);

        ObjectPropertyAssertions<RetryPolicyConfig> assertions =
            ObjectPropertyAssertions(mResult.getRetryPolicyConfig())
                .withFinalFieldOnly(false);

        assertions.checkField("maxIntervalSeconds", 600);
        assertions.checkField("exponentialMultiplier", 1);
        assertions.checkAll();
    }

    @Test
    public void testHasData() throws Exception {
        final int maxInterval = 1000;
        final int exponentialMultiplier = 2;
        StartupJsonMock startupJsonMock = new StartupJsonMock();
        startupJsonMock.addRetryPolicyConfig(maxInterval, exponentialMultiplier);

        mParser.parse(mResult, startupJsonMock);

        ObjectPropertyAssertions<RetryPolicyConfig> assertions =
            ObjectPropertyAssertions(mResult.getRetryPolicyConfig())
                .withFinalFieldOnly(false);

        assertions.checkField("maxIntervalSeconds", maxInterval);
        assertions.checkField("exponentialMultiplier", exponentialMultiplier);
        assertions.checkAll();
    }
}
