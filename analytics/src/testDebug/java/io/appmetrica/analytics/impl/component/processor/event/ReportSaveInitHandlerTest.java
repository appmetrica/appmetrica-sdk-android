package io.appmetrica.analytics.impl.component.processor.event;

import android.content.Context;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.gradle.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportSaveInitHandlerTest extends CommonTest {

    private static final String KEY_PRELOAD_INFO = "preloadInfo";
    private static final String KEY_APP_INSTALLER = "appInstaller";
    private static final String KEY_APP_INSTALLATION_INITIATOR = "appInstallationInitiator";

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private ReportRequestConfig mReportRequestConfig;
    @Mock
    private EventSaver mEventSaver;
    @Mock
    private PreloadInfoStorage mPreloadInfoStorage;
    private PreloadInfoState mPreloadInfoState;
    @Mock
    private SafePackageManager mSafePackageManager;
    @Mock
    private TimeProvider timeProvider;
    @Captor
    private ArgumentCaptor<CoreServiceEvent> mReportCaptor;
    private Context mContext;
    private final String mComponentIdPackage = "other.component.package";
    private final String mInstaller = "yandex.store";
    private final String mInitiator = "com.android.shell";
    private final Long currentTime = 100500L;
    private ReportSaveInitHandler mReportSaveInitHandler;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mPreloadInfoState = new PreloadInfoState("11", new JSONObject(), true, false, DistributionSource.APP);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(false);
        when(mComponent.getFreshReportRequestConfig()).thenReturn(mReportRequestConfig);
        ComponentId componentId = mock(ComponentId.class);
        when(componentId.getPackage()).thenReturn(mComponentIdPackage);
        when(mComponent.getComponentId()).thenReturn(componentId);
        when(mComponent.getContext()).thenReturn(mContext);
        assertThat(mContext.getPackageName()).isNotEqualTo(mComponentIdPackage);
        when(mPreloadInfoStorage.retrieveData()).thenReturn(mPreloadInfoState);
        when(timeProvider.currentTimeMillis()).thenReturn(currentTime);
        mReportSaveInitHandler = new ReportSaveInitHandler(
            mComponent,
            vitalComponentDataProvider,
            mPreloadInfoStorage,
            mSafePackageManager,
            timeProvider
        );
    }

    @Test
    public void testProcessShouldSaveInitReportIfInitNotYetSend() {
        mReportSaveInitHandler.process(new CoreServiceEvent());

        ArgumentCaptor<CoreServiceEvent> arg = ArgumentCaptor.forClass(CoreServiceEvent.class);

        verify(mEventSaver, times(1)).identifyAndSaveReport(arg.capture());
        assertThat(arg.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_INIT.getTypeId());
        verify(vitalComponentDataProvider, times(1)).setInitEventDone(true);
        verify(vitalComponentDataProvider, times(1))
            .setExternalAttributionWindowStart(currentTime);
    }

    @Test
    public void testProcessDoNothingIfInitAlreadySent() {
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(true);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, never()).identifyAndSaveReport(any(CoreServiceEvent.class));
        verify(vitalComponentDataProvider, never()).setInitEventDone(anyBoolean());
        verify(vitalComponentDataProvider, never()).setExternalAttributionWindowStart(anyLong());
    }

    @Test
    public void testProcessSendInitEventWithPreloadInfo() throws JSONException {
        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        JSONAssert.assertEquals(
            mPreloadInfoState.toEventJson(),
            new JSONObject(mReportCaptor.getValue().getValue()).getJSONObject(KEY_PRELOAD_INFO),
            true
        );
    }

    @Test
    public void testProcessSendInitEventWithoutPreloadInfo() throws JSONException {
        mPreloadInfoState = new PreloadInfoState(null, new JSONObject(), false, false, DistributionSource.UNDEFINED);
        when(mPreloadInfoStorage.retrieveData()).thenReturn(mPreloadInfoState);
        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optJSONObject(KEY_PRELOAD_INFO)).isNull();
    }

    @Test
    public void testProcessSendInitEventWithPackageInstaller() throws JSONException {
        when(mSafePackageManager.getInstallerPackageName(mContext)).thenReturn(mInstaller);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never()).getInstallerPackageName(mContext, mComponentIdPackage);
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLER)).isEqualTo(mInstaller);
    }

    @Test
    public void testProcessSendInitEventWithoutPackageInstaller() throws JSONException {
        when(mSafePackageManager.getInstallerPackageName(mContext)).thenReturn(null);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never()).getInstallerPackageName(mContext, mComponentIdPackage);
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLER)).isEmpty();
    }

    @Test
    public void processSendInitEventWithPackageInstallationInitiator() throws JSONException {
        when(mSafePackageManager.getInstallationInitiatorPackageName(mContext)).thenReturn(mInitiator);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never())
            .getInstallationInitiatorPackageName(mContext, mComponentIdPackage);
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLATION_INITIATOR))
            .isEqualTo(mInitiator);
    }

    @Test
    public void processSendInitEventWithoutPackageInstallationInitiator() throws JSONException {
        when(mSafePackageManager.getInstallationInitiatorPackageName(mContext)).thenReturn(null);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never())
            .getInstallationInitiatorPackageName(mContext, mComponentIdPackage);
        JSONObject value = new JSONObject(mReportCaptor.getValue().getValue());
        assertThat(value.has(KEY_APP_INSTALLATION_INITIATOR)).isTrue();
        assertThat(value.optString(KEY_APP_INSTALLATION_INITIATOR)).isEmpty();
    }

    @Test
    public void processSendInitEventWithEmptyPackageInstallationInitiator() throws JSONException {
        when(mSafePackageManager.getInstallationInitiatorPackageName(mContext)).thenReturn("");

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never())
            .getInstallationInitiatorPackageName(mContext, mComponentIdPackage);
        JSONObject value = new JSONObject(mReportCaptor.getValue().getValue());
        assertThat(value.has(KEY_APP_INSTALLATION_INITIATOR)).isTrue();
        assertThat(value.optString(KEY_APP_INSTALLATION_INITIATOR)).isEmpty();
    }

    @Test
    public void testProcessShouldNotBreakEventProcessing() {
        assertThat(mReportSaveInitHandler.process(new CoreServiceEvent())).isFalse();
    }

    @Test
    public void testProcessShouldSaveUpdateReportIfInitNotSendYet() {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();

        mReportSaveInitHandler.process(new CoreServiceEvent());

        ArgumentCaptor<CoreServiceEvent> arg = ArgumentCaptor.forClass(CoreServiceEvent.class);

        verify(mEventSaver, times(1)).identifyAndSaveReport(arg.capture());
        assertThat(arg.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId());
    }

    @Test
    public void testProcessShouldSaveInitDoneForUpdateReport() {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(vitalComponentDataProvider, times(1)).setInitEventDone(true);
        verify(vitalComponentDataProvider, times(1))
            .setExternalAttributionWindowStart(currentTime);
    }

    @Test
    public void processSendUpdateEventWithPackageInstallationInitiator() throws JSONException {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();
        when(mSafePackageManager.getInstallationInitiatorPackageName(mContext)).thenReturn(mInitiator);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never())
            .getInstallationInitiatorPackageName(mContext, mComponentIdPackage);
        assertThat(mReportCaptor.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId());
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLATION_INITIATOR))
            .isEqualTo(mInitiator);
    }

    @Test
    public void processSendUpdateEventWithoutPackageInstallationInitiator() throws JSONException {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();
        when(mSafePackageManager.getInstallationInitiatorPackageName(mContext)).thenReturn(null);

        mReportSaveInitHandler.process(new CoreServiceEvent());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        verify(mSafePackageManager, never())
            .getInstallationInitiatorPackageName(mContext, mComponentIdPackage);
        JSONObject value = new JSONObject(mReportCaptor.getValue().getValue());
        assertThat(value.has(KEY_APP_INSTALLATION_INITIATOR)).isTrue();
        assertThat(value.optString(KEY_APP_INSTALLATION_INITIATOR)).isEmpty();
    }
}
