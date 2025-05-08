package io.appmetrica.analytics.impl.component;

import android.location.Location;
import android.os.ResultReceiver;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class CommonArgumentsTest extends CommonTest {

    @Test
    public void testReporterArgumentsDefaultConstructor() throws IllegalAccessException {
        ObjectPropertyAssertions<CommonArguments.ReporterArguments> assertions =
            ObjectPropertyAssertions(new CommonArguments.ReporterArguments());
        Object nullVariable = null;
        assertions.checkField("apiKey", nullVariable);
        assertions.checkField("dispatchPeriod", nullVariable);
        assertions.checkField("manualLocation", nullVariable);
        assertions.checkField("logEnabled", nullVariable);
        assertions.checkField("dataSendingEnabled", nullVariable);
        assertions.checkField("sessionTimeout", nullVariable);
        assertions.checkField("locationTracking", nullVariable);
        assertions.checkField("maxReportsCount", nullVariable);
        assertions.checkField("firstActivationAsUpdate", nullVariable);
        assertions.checkField("clidsFromClient", nullVariable);
        assertions.checkField("maxReportsInDbCount", nullVariable);
        assertions.checkField("nativeCrashesEnabled", nullVariable);
        assertions.checkField("revenueAutoTrackingEnabled", nullVariable);
        assertions.checkField("advIdentifiersTrackingEnabled", nullVariable);
        assertions.checkAll();
    }

    @Test
    public void testReporterArgumentsConstructor() throws IllegalAccessException {
        String apiKey = "apiKey";
        boolean locationTracking = false;
        Location manualLocation = null;
        boolean firstActivationAsUpdate = true;
        int sessionTimeout = 122;
        int maxReportsCount = 11;
        int dispatchPeriod = 222;
        boolean logEnabled = false;
        boolean dataSendingEnabled = true;
        int maxReportsInDbCount = 2000;
        boolean nativeCrashesEnabled = true;
        boolean revenueAutoTrackingEnabled = false;
        boolean advIdentifiersTrackingEnabled = false;
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        ObjectPropertyAssertions<CommonArguments.ReporterArguments> assertions =
            ObjectPropertyAssertions(new CommonArguments.ReporterArguments(
                apiKey,
                locationTracking,
                manualLocation,
                firstActivationAsUpdate,
                sessionTimeout,
                maxReportsCount,
                dispatchPeriod,
                logEnabled,
                dataSendingEnabled,
                clids,
                maxReportsInDbCount,
                nativeCrashesEnabled,
                revenueAutoTrackingEnabled,
                advIdentifiersTrackingEnabled
            ));
        assertions.checkField("apiKey", apiKey);
        assertions.checkField("dispatchPeriod", dispatchPeriod);
        assertions.checkField("manualLocation", manualLocation);
        assertions.checkField("logEnabled", logEnabled);
        assertions.checkField("dataSendingEnabled", dataSendingEnabled);
        assertions.checkField("sessionTimeout", sessionTimeout);
        assertions.checkField("locationTracking", locationTracking);
        assertions.checkField("maxReportsCount", maxReportsCount);
        assertions.checkField("firstActivationAsUpdate", firstActivationAsUpdate);
        assertions.checkField("clidsFromClient", clids);
        assertions.checkField("maxReportsInDbCount", maxReportsInDbCount);
        assertions.checkField("nativeCrashesEnabled", nativeCrashesEnabled);
        assertions.checkField("revenueAutoTrackingEnabled", revenueAutoTrackingEnabled);
        assertions.checkField("advIdentifiersTrackingEnabled", advIdentifiersTrackingEnabled);
        assertions.checkAll();
    }

    @Test
    public void testCommonArgumentsConstructor() throws IllegalAccessException {
        String apiKey = "apiKey";
        boolean locationTracking = false;
        boolean advIdentifiersTrackingEnabled = false;
        Location manualLocation = null;
        boolean firstActivationAsUpdate = true;
        int sessionTimeout = 122;
        int maxReportsCount = 11;
        int maxReportsInDbCount = 2000;
        int dispatchPeriod = 222;
        boolean logEnabled = false;
        boolean dataSendingEnabled = true;
        boolean nativeCrashesEnabled = true;
        boolean revenueAutoTrackingEnabled = false;

        boolean hasNewCustomHosts = false;
        List<String> newCustomHosts = null;
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        String distributionReferrer = "distributionReferrer";
        String referrerSource = "broadcast";

        ProcessConfiguration processConfiguration = mock(ProcessConfiguration.class);
        CounterConfiguration counterConfiguration = mock(CounterConfiguration.class);

        doReturn(apiKey).when(counterConfiguration).getApiKey();
        doReturn(locationTracking).when(counterConfiguration).isLocationTrackingEnabled();
        doReturn(manualLocation).when(counterConfiguration).getManualLocation();
        doReturn(advIdentifiersTrackingEnabled).when(counterConfiguration).isAdvIdentifiersTrackingEnabled();
        doReturn(firstActivationAsUpdate).when(counterConfiguration).isFirstActivationAsUpdate();
        doReturn(sessionTimeout).when(counterConfiguration).getSessionTimeout();
        doReturn(maxReportsCount).when(counterConfiguration).getMaxReportsCount();
        doReturn(dispatchPeriod).when(counterConfiguration).getDispatchPeriod();
        doReturn(logEnabled).when(counterConfiguration).isLogEnabled();
        doReturn(dataSendingEnabled).when(counterConfiguration).getDataSendingEnabled();
        doReturn(maxReportsInDbCount).when(counterConfiguration).getMaxReportsInDbCount();
        doReturn(nativeCrashesEnabled).when(counterConfiguration).getReportNativeCrashesEnabled();
        doReturn(revenueAutoTrackingEnabled).when(counterConfiguration).isRevenueAutoTrackingEnabled();

        doReturn(distributionReferrer).when(processConfiguration).getDistributionReferrer();
        doReturn(referrerSource).when(processConfiguration).getInstallReferrerSource();
        doReturn(hasNewCustomHosts).when(processConfiguration).hasCustomHosts();
        doReturn(newCustomHosts).when(processConfiguration).getCustomHosts();
        doReturn(clids).when(processConfiguration).getClientClids();

        CommonArguments commonArguments = new CommonArguments(
            new ClientConfiguration(
                processConfiguration,
                counterConfiguration
            )
        );
        ObjectPropertyAssertions<CommonArguments.ReporterArguments> assertions =
            ObjectPropertyAssertions(
                commonArguments.componentArguments
            );

        assertions.checkField("apiKey", apiKey);
        assertions.checkField("dispatchPeriod", dispatchPeriod);
        assertions.checkField("manualLocation", manualLocation);
        assertions.checkField("logEnabled", logEnabled);
        assertions.checkField("dataSendingEnabled", dataSendingEnabled);
        assertions.checkField("sessionTimeout", sessionTimeout);
        assertions.checkField("locationTracking", locationTracking);
        assertions.checkField("maxReportsCount", maxReportsCount);
        assertions.checkField("firstActivationAsUpdate", firstActivationAsUpdate);
        assertions.checkField("clidsFromClient", clids);
        assertions.checkField("maxReportsInDbCount", maxReportsInDbCount);
        assertions.checkField("nativeCrashesEnabled", nativeCrashesEnabled);
        assertions.checkField("revenueAutoTrackingEnabled", revenueAutoTrackingEnabled);
        assertions.checkField("advIdentifiersTrackingEnabled", advIdentifiersTrackingEnabled);
        assertions.checkAll();

        ObjectPropertyAssertions<StartupRequestConfig.Arguments> startupAssertions =
            ObjectPropertyAssertions(commonArguments.startupArguments);
        startupAssertions.checkField("hasNewCustomHosts", hasNewCustomHosts);
        startupAssertions.checkField("newCustomHosts", newCustomHosts);
        startupAssertions.checkField("clientClids", clids);
        startupAssertions.checkField("distributionReferrer", distributionReferrer);
        startupAssertions.checkField("installReferrerSource", referrerSource);
        startupAssertions.checkAll();
    }

    @Test
    public void testCommonArgumentsConstructorFromArguments() {

        StartupRequestConfig.Arguments startupArguments = new StartupRequestConfig.Arguments();
        CommonArguments.ReporterArguments componentArguments = new CommonArguments.ReporterArguments();
        ResultReceiver dataReceiver = mock(ResultReceiver.class);
        CommonArguments commonArguments = new CommonArguments(
            startupArguments, componentArguments, dataReceiver
        );

        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(commonArguments.startupArguments).as("startupArguments").isSameAs(startupArguments);
        softAssertions.assertThat(commonArguments.componentArguments).as("commonArguments").isSameAs(componentArguments);
        softAssertions.assertThat(commonArguments.dataResultReceiver).as("dataResultReceiver").isSameAs(dataReceiver);

        softAssertions.assertAll();
    }
}
