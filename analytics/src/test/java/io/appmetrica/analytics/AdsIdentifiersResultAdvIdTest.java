package io.appmetrica.analytics;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AdsIdentifiersResultAdvIdTest extends CommonTest {

    @Test
    public void testConstructor() throws Exception {
        final String identifier = "some identifier";
        final AdsIdentifiersResult.Details details = AdsIdentifiersResult.Details.NO_STARTUP;
        String error = "some error";
        AdsIdentifiersResult.AdvId advId = new AdsIdentifiersResult.AdvId(identifier, details, error);
        ObjectPropertyAssertions<AdsIdentifiersResult.AdvId> assertions = ObjectPropertyAssertions(advId);
        assertions.checkField("advId", identifier);
        assertions.checkField("details", details);
        assertions.checkField("errorExplanation", error);
        assertions.checkAll();
    }
}
