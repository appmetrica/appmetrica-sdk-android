package io.appmetrica.analytics.coreapi.identifiers;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AdTrackingInfoResultTest extends CommonTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        AdTrackingInfoResult adTrackingInfoResult = new AdTrackingInfoResult();
        ObjectPropertyAssertions<AdTrackingInfoResult> assertions = ObjectPropertyAssertions(adTrackingInfoResult);
        assertions.checkField("mAdTrackingInfo", (AdTrackingInfo) null);
        assertions.checkField("mStatus", IdentifierStatus.UNKNOWN);
        assertions.checkField("mErrorExplanation", "identifier info has never been updated");
        assertions.checkAll();
    }

    @Test
    public void testConstructorFilled() throws Exception {
        AdTrackingInfo adTrackingInfo = mock(AdTrackingInfo.class);
        IdentifierStatus status = IdentifierStatus.OK;
        String error = "error";
        AdTrackingInfoResult adTrackingInfoResult = new AdTrackingInfoResult(adTrackingInfo, status, error);
        ObjectPropertyAssertions<AdTrackingInfoResult> assertions = ObjectPropertyAssertions(adTrackingInfoResult);
        assertions.checkField("mAdTrackingInfo", adTrackingInfo);
        assertions.checkField("mStatus", status);
        assertions.checkField("mErrorExplanation", error);
        assertions.checkAll();
    }

    @Test
    public void testConstructorNullable() throws Exception {
        AdTrackingInfoResult adTrackingInfoResult = new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null);
        ObjectPropertyAssertions<AdTrackingInfoResult> assertions = ObjectPropertyAssertions(adTrackingInfoResult)
                .withIgnoredFields("mStatus");
        assertions.checkField("mAdTrackingInfo", (AdTrackingInfo) null);
        assertions.checkField("mErrorExplanation", (String) null);
        assertions.checkAll();
    }

    @Test
    public void testIsValidNullAdTrackingInfo() {
        assertThat(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "error").isValid()).isFalse();
    }

    @Test
    public void testIsValidNullAdvId() {
        assertThat(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, null, false),
                IdentifierStatus.OK,
                null
        ).isValid()).isFalse();
    }

    @Test
    public void testIsValidValid() {
        assertThat(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "some id", false),
                IdentifierStatus.OK,
                "error"
        ).isValid()).isTrue();
    }

    @Test
    public void getProviderUnavailableResult() throws IllegalAccessException {
        String errorMessage = "error message";
        ObjectPropertyAssertions(AdTrackingInfoResult.getProviderUnavailableResult(errorMessage))
                .checkFieldIsNull("mAdTrackingInfo")
                .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
                .checkField("mErrorExplanation", errorMessage)
                .checkAll();
    }
}
