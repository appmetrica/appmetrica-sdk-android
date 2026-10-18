package io.appmetrica.analytics.impl.startup;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.StartupParamsItem;
import io.appmetrica.analytics.StartupParamsItemStatus;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.gradle.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupHelperTest extends CommonTest {

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

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
        verify(reportsHandler, never()).reportStartupEvent(anyList(), anyMap(), anyBoolean());
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
            eq(null),
            eq(false)
        );
    }

    @Test
    public void sendStartupIfNeededWhenShouldNotSend() {
        when(startupParams.shouldSendStartup()).thenReturn(false);
        startupHelper.initialStartupSent = true;

        startupHelper.sendStartupIfNeeded();

        verifyNoInteractions(reportsHandler);
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
    public void setLibraryAdapterCustomHosts() {
        List<String> customHosts = Arrays.asList("host1", "host2");

        startupHelper.setLibraryAdapterCustomHosts(customHosts);

        verify(reportsHandler).setLibraryAdapterCustomHosts(customHosts);
        verify(reportsHandler).reportStartupEvent(
            eq(allIdentifiers),
            eq(null),
            eq(false)
        );
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

    @Test
    public void explicitResultCompletesReadySubsetsAndOnlyItsIncompleteCallback() {
        List<String> uuidIdentifiers = Collections.singletonList(StartupParamsCallback.APPMETRICA_UUID);
        List<String> deviceIdentifiers = Arrays.asList(
            StartupParamsCallback.APPMETRICA_UUID, StartupParamsCallback.APPMETRICA_DEVICE_ID
        );
        List<String> hashIdentifiers = Collections.singletonList(StartupParamsCallback.APPMETRICA_DEVICE_ID_HASH);
        StartupParamsCallback addressedCallback = mock(StartupParamsCallback.class);
        StartupParamsCallback pendingCallback = mock(StartupParamsCallback.class);
        StartupParamsItem uuid = new StartupParamsItem("uuid", StartupParamsItemStatus.OK, null);
        when(startupParams.shouldSendStartup(anyList())).thenReturn(true);
        doAnswer(invocation -> {
            List<String> identifiers = invocation.getArgument(0);
            Map<String, StartupParamsItem> result = invocation.getArgument(1);
            if (identifiers.contains(StartupParamsCallback.APPMETRICA_UUID)) {
                result.put(StartupParamsCallback.APPMETRICA_UUID, uuid);
            }
            return null;
        }).when(startupParams).putToMap(anyList(), anyMap());
        startupHelper.requestStartupParams(callback, uuidIdentifiers, clientClids);
        startupHelper.requestStartupParams(addressedCallback, deviceIdentifiers, clientClids);
        startupHelper.requestStartupParams(pendingCallback, hashIdentifiers, clientClids);
        ArgumentCaptor<ResultReceiver> receiverCaptor = ArgumentCaptor.forClass(ResultReceiver.class);
        verify(reportsHandler).reportStartupEvent(
            eq(deviceIdentifiers), receiverCaptor.capture(), eq(clientClids), eq(true)
        );
        when(startupParams.containsIdentifiers(uuidIdentifiers)).thenReturn(true);
        Bundle error = new Bundle();
        StartupError.NETWORK.toBundle(error);

        receiverCaptor.getValue().send(0, error);

        ArgumentCaptor<StartupParamsCallback.Result> ready =
            ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(callback).onReceive(ready.capture());
        assertThat(ready.getValue().parameters).containsOnlyKeys(StartupParamsCallback.APPMETRICA_UUID);
        assertThat(ready.getValue().uuid).isEqualTo("uuid");
        ArgumentCaptor<StartupParamsCallback.Result> partial =
            ArgumentCaptor.forClass(StartupParamsCallback.Result.class);
        verify(addressedCallback).onRequestError(eq(StartupParamsCallback.Reason.NETWORK), partial.capture());
        assertThat(partial.getValue().parameters).containsOnlyKeys(StartupParamsCallback.APPMETRICA_UUID);
        assertThat(partial.getValue().uuid).isEqualTo("uuid");
        assertThat(partial.getValue().deviceId).isNull();
        verifyNoInteractions(pendingCallback);
        assertThat(startupHelper.getStartupAllParamsCallbacks()).containsOnlyKeys(pendingCallback);
        verify(reportsHandler).onStartupRequestStarted();
        verify(reportsHandler, never()).onStartupRequestFinished();

        when(startupParams.containsIdentifiers(hashIdentifiers)).thenReturn(true);
        startupHelper.processResultFromResultReceiver(new Bundle());
        startupHelper.processResultFromResultReceiver(new Bundle());
        verify(callback).onReceive(any(StartupParamsCallback.Result.class));
        verify(addressedCallback).onRequestError(eq(StartupParamsCallback.Reason.NETWORK), any());
        verify(pendingCallback).onReceive(any(StartupParamsCallback.Result.class));
        verify(reportsHandler).onStartupRequestFinished();
        assertThat(startupHelper.getStartupAllParamsCallbacks()).isEmpty();
    }

    @Test
    public void generalErrorKeepsIncompleteCallbacksPending() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(true);
        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);
        Bundle error = new Bundle();
        StartupError.NETWORK.toBundle(error);

        startupHelper.processResultFromResultReceiver(error);

        verifyNoInteractions(callback);
        assertThat(startupHelper.getStartupAllParamsCallbacks()).containsOnlyKeys(callback);
        verify(reportsHandler).onStartupRequestStarted();
        verify(reportsHandler, never()).onStartupRequestFinished();
        when(startupParams.containsIdentifiers(allIdentifiers)).thenReturn(true);
        startupHelper.processResultFromResultReceiver(error);
        startupHelper.processResultFromResultReceiver(error);
        verify(callback).onReceive(any(StartupParamsCallback.Result.class));
        verify(callback, never()).onRequestError(any(), any());
        verify(reportsHandler).onStartupRequestFinished();
        assertThat(startupHelper.getStartupAllParamsCallbacks()).isEmpty();
    }

    @Test
    public void generalResultChecksPendingSubsetOnce() {
        when(startupParams.shouldSendStartup(allIdentifiers)).thenReturn(true);
        startupHelper.requestStartupParams(callback, allIdentifiers, clientClids);
        clearInvocations(startupParams);

        startupHelper.processResultFromResultReceiver(new Bundle());

        verify(startupParams, times(1)).containsIdentifiers(allIdentifiers);
        verifyNoInteractions(callback);
        assertThat(startupHelper.getStartupAllParamsCallbacks()).containsOnlyKeys(callback);
    }

    @Test
    public void generalResultUpdatesCacheAndModulesBeforeReentrantLocalCallback() {
        List<String> identifiers = Collections.singletonList(StartupParamsCallback.APPMETRICA_UUID);
        StartupParamsCallback reentrantCallback = mock(StartupParamsCallback.class);
        when(startupParams.shouldSendStartup(identifiers)).thenReturn(true);
        startupHelper.requestStartupParams(callback, identifiers, clientClids);
        when(startupParams.containsIdentifiers(identifiers)).thenReturn(true);
        when(startupParams.shouldSendStartup(identifiers)).thenReturn(false);
        doAnswer(invocation -> {
            startupHelper.requestStartupParams(reentrantCallback, identifiers, clientClids);
            return null;
        }).when(callback).onReceive(any());

        startupHelper.processResultFromResultReceiver(new Bundle());

        InOrder order = inOrder(startupParams, clientServiceLocatorRule.modulesController, callback, reentrantCallback);
        order.verify(startupParams).updateAllParamsByReceiver(any());
        order.verify(clientServiceLocatorRule.modulesController).notifyModulesWithConfig(any(), any());
        order.verify(callback).onReceive(any());
        order.verify(reentrantCallback).onReceive(any());
        verify(reportsHandler).reportStartupEvent(
            eq(identifiers), any(ResultReceiver.class), eq(clientClids), eq(true)
        );
        verify(reportsHandler).onStartupRequestStarted();
        verify(reportsHandler).onStartupRequestFinished();
        assertThat(startupHelper.getStartupAllParamsCallbacks()).isEmpty();
    }
}
