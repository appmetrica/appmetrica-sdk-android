package io.appmetrica.analytics.impl.component.processor.event;

import android.content.Context;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.events.EventListener;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private ReportRequestConfig mReportRequestConfig;
    @Mock
    private EventSaver mEventSaver;
    @Mock
    private EventListener mEventListener;
    @Mock
    private PreloadInfoStorage mPreloadInfoStorage;
    private PreloadInfoState mPreloadInfoState;
    @Mock
    private SafePackageManager mSafePackageManager;
    @Captor
    private ArgumentCaptor<CounterReport> mReportCaptor;
    private Context mContext;
    private final String mPackage = "test.package";
    private final String mInstaller = "yandex.store";
    private ReportSaveInitHandler mReportSaveInitHandler;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mPreloadInfoState = new PreloadInfoState("11", new JSONObject(), true, false, DistributionSource.APP);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(false);
        when(mComponent.getFreshReportRequestConfig()).thenReturn(mReportRequestConfig);
        when(mComponent.getReportsListener()).thenReturn(mEventListener);
        ComponentId componentId = mock(ComponentId.class);
        when(componentId.getPackage()).thenReturn(mPackage);
        when(mComponent.getComponentId()).thenReturn(componentId);
        when(mComponent.getContext()).thenReturn(mContext);
        when(mPreloadInfoStorage.retrieveData()).thenReturn(mPreloadInfoState);
        mReportSaveInitHandler = new ReportSaveInitHandler(mComponent, vitalComponentDataProvider, mPreloadInfoStorage, mSafePackageManager);
    }

    @Test
    public void testProcessShouldSaveInitReportIfInitNotYetSend() {
        mReportSaveInitHandler.process(new CounterReport());

        ArgumentCaptor<CounterReport> arg = ArgumentCaptor.forClass(CounterReport.class);

        verify(mEventSaver, times(1)).identifyAndSaveReport(arg.capture());
        assertThat(arg.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_INIT.getTypeId());
        verify(vitalComponentDataProvider, times(1)).setInitEventDone(true);
    }

    @Test
    public void testProcessDoNothingIfInitAlreadySent() {
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(true);

        ArgumentCaptor<CounterReport> arg2 = ArgumentCaptor.forClass(CounterReport.class);
        verify(mEventSaver, never()).identifyAndSaveReport(arg2.capture());

        verify(vitalComponentDataProvider, never()).setInitEventDone(anyBoolean());
    }

    @Test
    public void testProcessSendInitEventWithPreloadInfo() throws JSONException {
        mReportSaveInitHandler.process(new CounterReport());

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
        mReportSaveInitHandler.process(new CounterReport());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optJSONObject(KEY_PRELOAD_INFO)).isNull();
    }

    @Test
    public void testProcessSendInitEventWithPackageInstaller() throws JSONException {
        when(mSafePackageManager.getInstallerPackageName(mContext, mPackage)).thenReturn(mInstaller);

        mReportSaveInitHandler.process(new CounterReport());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLER)).isEqualTo(mInstaller);
    }

    @Test
    public void testProcessSendInitEventWithoutPackageInstaller() throws JSONException {
        when(mSafePackageManager.getInstallerPackageName(mContext, mPackage)).thenReturn(null);

        mReportSaveInitHandler.process(new CounterReport());

        verify(mEventSaver, times(1)).identifyAndSaveReport(mReportCaptor.capture());
        assertThat(new JSONObject(mReportCaptor.getValue().getValue()).optString(KEY_APP_INSTALLER)).isEmpty();
    }

    @Test
    public void testProcessShouldNotBreakEventProcessing() {
        assertThat(mReportSaveInitHandler.process(new CounterReport())).isFalse();
    }

    @Test
    public void testProcessShouldSaveUpdateReportIfInitNotSendYet() {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();

        mReportSaveInitHandler.process(new CounterReport());

        ArgumentCaptor<CounterReport> arg = ArgumentCaptor.forClass(CounterReport.class);

        verify(mEventSaver, times(1)).identifyAndSaveReport(arg.capture());
        assertThat(arg.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId());
    }

    @Test
    public void testProcessShouldSaveInitDoneForUpdateReport() {
        doReturn(true).when(mReportRequestConfig).isFirstActivationAsUpdate();

        mReportSaveInitHandler.process(new CounterReport());

        verify(vitalComponentDataProvider, times(1)).setInitEventDone(true);
    }
}
