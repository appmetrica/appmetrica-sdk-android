package io.appmetrica.analytics.impl.startup;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupHelperTest extends CommonTest {

    @Mock
    private ReportsHandler reportsHandler;
    @Mock
    private StartupParams startupParams;
    @Mock
    private Handler handler;
    @Mock
    private StartupParamsCallback callback;

    private StartupHelper startupHelper;

    private final List<String> allIdentifiers = Arrays.asList(
        Constants.StartupParamsCallbackKeys.UUID,
        Constants.StartupParamsCallbackKeys.DEVICE_ID,
        Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
        Constants.StartupParamsCallbackKeys.GET_AD_URL,
        Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
        Constants.StartupParamsCallbackKeys.CLIDS
    );

    private final Map<String, String> clientClids = new HashMap<String, String>() {{
        put("clid0", "222");
        put("clid1", "333");
    }};

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        startupHelper = new StartupHelper(reportsHandler, startupParams, handler);
    }

    @Test
    public void getUuid() {
        String uuid = "test-uuid";
        when(startupParams.getUuid()).thenReturn(uuid);
        assertThat(startupHelper.getUuid()).isEqualTo(uuid);
    }

    @Test
    public void getDeviceId() {
        String deviceId = "test-device-id";
        when(startupParams.getDeviceId()).thenReturn(deviceId);
        assertThat(startupHelper.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void getServerTimeOffsetSeconds() {
        long offset = 1234567890L;
        when(startupParams.getServerTimeOffsetSeconds()).thenReturn(offset);
        assertThat(startupHelper.getServerTimeOffsetSeconds()).isEqualTo(offset);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(startupParams.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(startupHelper.getCachedAdvIdentifiers()).isEqualTo(result);
    }

    @Test
    public void getFeatures() {
        FeaturesResult result = mock(FeaturesResult.class);
        when(startupParams.getFeatures()).thenReturn(result);
        assertThat(startupHelper.getFeatures()).isEqualTo(result);
    }

    @Test
    public void requestStartupParamsWhenShouldSendStartup() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(true);

        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);

        verify(startupParams).setClientClids(clientClids);
        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            any(ResultReceiver.class),
            eq(clientClids),
            eq(true)
        );
        verify(reportsHandler).onStartupRequestStarted();
    }

    @Test
    public void requestStartupParamsWhenShouldNotSendStartup() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(false);
        when(startupParams.containsIdentifiers(allIdentifiers)).thenReturn(true);

        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);

        verify(startupParams).setClientClids(clientClids);
        verify(reportsHandler, never()).reportStartupEvent(
            anyList(),
            any(ResultReceiver.class),
            anyMap(),
            anyBoolean()
        );
        verify(callback).onReceive(any(StartupParamsCallback.Result.class));
    }

    @Test
    public void requestStartupParamsNotifiesOnSuccess() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(true);
        when(startupParams.containsIdentifiers(allIdentifiers)).thenReturn(true);

        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);

        ArgumentCaptor<ResultReceiver> receiverCaptor = ArgumentCaptor.forClass(ResultReceiver.class);
        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            receiverCaptor.capture(),
            eq(clientClids),
            eq(true)
        );

        Bundle resultData = new Bundle();
        receiverCaptor.getValue().send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, resultData);

        verify(callback).onReceive(any(StartupParamsCallback.Result.class));
        verify(reportsHandler).onStartupRequestFinished();
    }

    @Test
    public void requestStartupParamsNotifiesOnError() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(true);
        when(startupParams.containsIdentifiers(allIdentifiers)).thenReturn(false);

        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);

        ArgumentCaptor<ResultReceiver> receiverCaptor = ArgumentCaptor.forClass(ResultReceiver.class);
        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            receiverCaptor.capture(),
            eq(clientClids),
            eq(true)
        );

        Bundle resultData = new Bundle();
        StartupError.NETWORK.toBundle(resultData);
        receiverCaptor.getValue().send(0, resultData);

        verify(callback).onRequestError(
            eq(StartupParamsCallback.Reason.NETWORK),
            any(StartupParamsCallback.Result.class)
        );
        verify(reportsHandler).onStartupRequestFinished();
    }

    @Test
    public void sendStartupIfNeededWhenInitialStartupNotSent() {
        when(startupParams.shouldSendStartup()).thenReturn(false);
        assertThat(startupHelper.initialStartupSent).isFalse();

        startupHelper.sendStartupIfNeeded();

        assertThat(startupHelper.initialStartupSent).isTrue();
        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            any(ResultReceiver.class),
            eq(null),
            eq(false)
        );
    }

    @Test
    public void sendStartupIfNeededWhenShouldSendStartup() {
        when(startupParams.shouldSendStartup()).thenReturn(true);
        startupHelper.initialStartupSent = true;

        startupHelper.sendStartupIfNeeded();

        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            any(ResultReceiver.class),
            eq(null),
            eq(false)
        );
    }

    @Test
    public void sendStartupIfNeededWhenShouldNotSend() {
        when(startupParams.shouldSendStartup()).thenReturn(false);
        startupHelper.initialStartupSent = true;

        startupHelper.sendStartupIfNeeded();

        verify(reportsHandler, never()).reportStartupEvent(
            anyList(),
            any(ResultReceiver.class),
            anyMap(),
            anyBoolean()
        );
    }

    @Test
    public void setClids() {
        Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");

        startupHelper.setClids(clids);

        verify(reportsHandler).setClids(anyMap());
        verify(startupParams).setClientClids(anyMap());
    }

    @Test
    public void setClidsWithNull() {
        startupHelper.setClids(null);

        verify(reportsHandler, never()).setClids(anyMap());
        verify(startupParams, never()).setClientClids(anyMap());
    }

    @Test
    public void setClidsWithEmpty() {
        startupHelper.setClids(new HashMap<>());

        verify(reportsHandler, never()).setClids(anyMap());
        verify(startupParams, never()).setClientClids(anyMap());
    }

    @Test
    public void setCustomHosts() {
        List<String> customHosts = Arrays.asList("host1", "host2");
        when(startupParams.getCustomHosts()).thenReturn(null);

        startupHelper.setCustomHosts(customHosts);

        verify(startupParams).setCustomHosts(customHosts);
        verify(reportsHandler).setCustomHosts(customHosts);
    }

    @Test
    public void setDistributionReferrer() {
        String referrer = "test-referrer";

        startupHelper.setDistributionReferrer(referrer);

        verify(reportsHandler).setDistributionReferrer(referrer);
    }

    @Test
    public void setInstallReferrerSource() {
        String source = "test-source";

        startupHelper.setInstallReferrerSource(source);

        verify(reportsHandler).setInstallReferrerSource(source);
    }

    @Test
    public void processResultFromResultReceiver() {
        Bundle resultData = new Bundle();

        startupHelper.processResultFromResultReceiver(resultData);

        verify(startupParams).updateAllParamsByReceiver(any());
    }
}
