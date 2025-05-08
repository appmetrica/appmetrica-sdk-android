package io.appmetrica.analytics.impl.stub;

import android.location.Location;
import android.os.Bundle;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collections;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaImplStubTest extends CommonTest {

    @Mock
    private AppMetricaConfig appMetricaConfig;
    @Mock
    private DeferredDeeplinkParametersListener deferredDeeplinkParametersListener;
    @Mock
    private DeferredDeeplinkListener deferredDeeplinkListener;
    @Mock
    private ReporterConfig reporterInternalConfig;
    @Mock
    private StartupParamsCallback startupParamsCallback;
    @Mock
    private Bundle bundle;
    @Mock
    private Location location;

    private AppMetricaImplStub stub;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        stub = new AppMetricaImplStub();
    }

    @Test
    public void activate() {
        stub.activate(appMetricaConfig);
        verifyNoMoreInteractions(appMetricaConfig, appMetricaConfig);
    }

    @Test
    public void getMainReporterApiConsumerProvider() {
        assertThat(stub.getMainReporterApiConsumerProvider().getMainReporter())
            .isInstanceOf(MainReporterStub.class);
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        stub.requestDeferredDeeplinkParameters(deferredDeeplinkParametersListener);
        verifyNoMoreInteractions(deferredDeeplinkListener);
    }

    @Test
    public void requestDeferredDeeplink() {
        stub.requestDeferredDeeplink(deferredDeeplinkListener);
        verifyNoMoreInteractions(deferredDeeplinkListener);
    }

    @Test
    public void activateReporter() {
        stub.activateReporter(reporterInternalConfig);
        verifyNoMoreInteractions(reporterInternalConfig);
    }

    @Test
    public void getReporter() {
        assertThat(stub.getReporter(reporterInternalConfig))
            .isNotNull()
            .isInstanceOf(ReporterExtendedStub.class);
    }

    @Test
    public void getDeviceId() {
        assertThat(stub.getDeviceId()).isNull();
    }

    @Test
    public void getCachedAdvIdentifiers() throws Exception {
        assertAdvIdentifiersResult(stub.getCachedAdvIdentifiers());
    }

    @Test
    public void getClids() {
        assertThat(stub.getClids()).isNull();
    }

    @Test
    public void requestStartupParamsWithStartupParamsCallback() {
        stub.requestStartupParams(
            startupParamsCallback,
            Collections.singletonList(Constants.StartupParamsCallbackKeys.UUID)
        );
        verify(startupParamsCallback).onRequestError(StartupParamsCallback.Reason.UNKNOWN, null);
    }

    @Test
    public void onReceiveResult() {
        stub.onReceiveResult(10, bundle);
        verifyNoMoreInteractions(bundle);
    }

    @Test
    public void setLocation() {
        stub.setLocation(location);
        verifyNoMoreInteractions(location);
    }

    @Test
    public void setLocationTracking() {
        stub.setLocationTracking(true);
    }

    @Test
    public void setDataSendingEnabled() {
        stub.setDataSendingEnabled(true);
    }

    @Test
    public void putAppEnvironmentValue() {
        stub.putAppEnvironmentValue("key", "value");
    }

    @Test
    public void clearAppEnvironment() {
        stub.clearAppEnvironment();
    }

    @Test
    public void putErrorEnvironmentValue() {
        stub.putErrorEnvironmentValue("key", "value");
    }

    @Test
    public void setUserProfileID() {
        stub.setUserProfileID("User Profile ID");
    }

    @Test
    public void getReporterFactory() {
        assertThat(stub.getReporterFactory())
            .isNotNull()
            .isInstanceOf(ReporterFactoryStub.class);
    }

    @Test
    public void getFeatures() throws Exception {
        ObjectPropertyAssertions(stub.getFeatures())
            .withPrivateFields(true)
            .checkFieldIsNull("libSslEnabled", "getLibSslEnabled")
            .checkAll();
    }

    private void assertAdvIdentifiersResult(AdvIdentifiersResult result) throws Exception {
        Consumer<ObjectPropertyAssertions<AdvIdentifiersResult.AdvId>> verifier =
            new Consumer<ObjectPropertyAssertions<AdvIdentifiersResult.AdvId>>() {
                @Override
                public void accept(ObjectPropertyAssertions<AdvIdentifiersResult.AdvId> assertions) {
                    try {
                        assertions.checkFieldIsNull("advId")
                            .checkField("details", AdvIdentifiersResult.Details.INTERNAL_ERROR)
                            .checkField("errorExplanation", "Device user is in locked state")
                            .checkAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        ObjectPropertyAssertions(result)
            .checkFieldRecursively("googleAdvId", verifier)
            .checkFieldRecursively("huaweiAdvId", verifier)
            .checkFieldRecursively("yandexAdvId", verifier)
            .checkAll();
    }
}
