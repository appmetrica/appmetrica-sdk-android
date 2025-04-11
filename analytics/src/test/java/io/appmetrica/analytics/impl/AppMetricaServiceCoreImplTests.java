package io.appmetrica.analytics.impl;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.service.ServiceCrashController;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreImplTests extends CommonTest {

    private Context mContext;
    @Mock
    private AppMetricaServiceCallback mCallback;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @Mock
    private CollectingFlags mCollectingFlags;
    @Mock
    private ReportConsumer mReportConsumer;
    @Mock
    private ApplicationStateProviderImpl mApplicationStateProvider;
    @Mock
    private ResultReceiver mResultReceiver;
    @Mock
    private FirstServiceEntryPointManager firstServiceEntryPointManager;
    @Mock
    private AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
    @Mock
    private ReportConsumer reportConsumer;
    @Mock
    private Resources resources;
    @Mock
    private Configuration configuration;
    @Mock
    private Configuration newConfiguration;
    private Intent intent;

    private StartupState mStartupState;
    private AppMetricaServiceCoreImpl mMetricaCore;

    private SdkEnvironmentHolder sdkEnvironmentHolder;

    @Captor
    private ArgumentCaptor<AppMetricaServiceLifecycle.LifecycleObserver> mLifecycleObserverCaptor;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();
    @Rule
    public MockedStaticRule<JsonHelper> mockedStaticRule = new MockedStaticRule<>(JsonHelper.class);
    @Rule
    public MockedConstructionRule<ReportProxy> reportProxyMockedConstructionRule = new MockedConstructionRule<>(ReportProxy.class);
    @Rule
    public MockedConstructionRule<CoreImplFirstCreateTaskLauncherProvider> firstCreateTaskLauncherProviderRule =
        new MockedConstructionRule<>(
            CoreImplFirstCreateTaskLauncherProvider.class,
            new MockedConstruction.MockInitializer<CoreImplFirstCreateTaskLauncherProvider>() {
                @Override
                public void prepare(CoreImplFirstCreateTaskLauncherProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getLauncher()).thenReturn(mock(CoreImplFirstCreateTaskLauncher.class));
                }
            }
        );
    @Rule
    public MockedConstructionRule<ServiceContextFacade> serviceContextFacadeMockedConstructionRule =
        new MockedConstructionRule<>(ServiceContextFacade.class);

    @Rule
    public MockedConstructionRule<ServiceCrashController> serviceCrashControllerMockedConstructionRule =
        new MockedConstructionRule<>(ServiceCrashController.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
                .thenReturn(mock(VitalCommonDataProvider.class));
        intent = new Intent();
        mContext = TestUtils.createMockedContext();

        doReturn(reportConsumer).when(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));

        when(mContext.getResources()).thenReturn(resources);
        when(resources.getConfiguration()).thenReturn(configuration);
        sdkEnvironmentHolder = GlobalServiceLocator.getInstance().getSdkEnvironmentHolder();

        mMetricaCore = new AppMetricaServiceCoreImpl(
            mContext,
            mCallback,
            mClientRepository,
            mAppMetricaServiceLifecycle,
            firstServiceEntryPointManager,
            mApplicationStateProvider,
            fieldsFactory
        );

        mStartupState = new StartupState.Builder(mCollectingFlags).build();
        GlobalServiceLocator.getInstance().getStartupStateHolder().onStartupStateChanged(mStartupState);
        mMetricaCore.onCreate();
        mMetricaCore.setReportConsumer(mReportConsumer);
    }

    @Test
    public void construction() {
        assertThat(reportProxyMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void onCreateDoesNotUpdateLocaleForFistTime() {
        verify(sdkEnvironmentHolder, never()).mayBeUpdateConfiguration(configuration);
    }

    @Test
    public void onCreateUpdatesLocaleForNonFirstTime() {
        mMetricaCore.onCreate();
        verify(sdkEnvironmentHolder).mayBeUpdateConfiguration(configuration);
    }

    @Test
    public void onConfigurationChanged() {
        mMetricaCore.onConfigurationChanged(newConfiguration);
        verify(sdkEnvironmentHolder).mayBeUpdateConfiguration(newConfiguration);
    }

    @Test
    public void testRemoveByPid() {
        ClientRepository repository = mock(ClientRepository.class);
        String packageName = UUID.randomUUID().toString();
        int pid = 100500;
        String psid = UUID.randomUUID().toString();
        Uri uri = new Uri.Builder().authority(packageName).
                path("client").
                appendQueryParameter("pid", String.valueOf(pid)).
                appendQueryParameter("psid", psid).build();

        mMetricaCore.setClientRepository(repository);
        mMetricaCore.removeClients(uri, packageName);
        verify(repository).remove(packageName, pid, psid);
    }

    @Test
    public void newClientConnectObserveDoesNotUpdateScreenSizeIfNoExtras() {
        touchNewClientConnectedObserver();
        verify(sdkEnvironmentHolder).mayBeUpdateScreenInfo(null);
    }

    @Test
    public void newClientConnectObserveUpdatesScreenSizeToNull() {
        intent.putExtra(ServiceUtils.EXTRA_SCREEN_SIZE, new Bundle());
        touchNewClientConnectedObserver();
        verify(sdkEnvironmentHolder).mayBeUpdateScreenInfo(null);
    }

    @Test
    public void newClientConnectObserveUpdatesScreenSize() {
        ScreenInfo screenInfo = mock(ScreenInfo.class);
        String screenInfoString = "Screen info json string";
        intent.putExtra(ServiceUtils.EXTRA_SCREEN_SIZE, screenInfoString);
        when(JsonHelper.screenInfoFromJsonString(screenInfoString)).thenReturn(screenInfo);
        touchNewClientConnectedObserver();
        verify(sdkEnvironmentHolder).mayBeUpdateScreenInfo(screenInfo);
    }

    @Test
    public void testResumeUserSessionForEmptyBundle() {
        testResumeUserSessionForInvalidBundle(new Bundle());
    }

    @Test
    public void testResumeUserSessionForBundleWithoutProcessConfiguration() {
        Bundle bundle = new Bundle();
        bundle.putString("Some bundle string key", "Some bundle string value");
        testResumeUserSessionForInvalidBundle(bundle);
    }

    @Test
    public void testResumeUserSessionForProcessConfigurationWithoutProcessId() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration(new ContentValues(), mResultReceiver);
        Bundle bundle = new Bundle();
        processConfiguration.toBundle(bundle);
        testResumeUserSessionForInvalidBundle(bundle);
    }

    private void testResumeUserSessionForInvalidBundle(Bundle bundle) {
        mMetricaCore.resumeUserSession(bundle);

        verifyNoMoreInteractions(mApplicationStateProvider);
    }

    @Test
    public void testResumeUserSession() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration(mContext, mResultReceiver);
        Bundle bundle = new Bundle();
        processConfiguration.toBundle(bundle);
        mMetricaCore.resumeUserSession(bundle);

        verify(mApplicationStateProvider).resumeUserSessionForPid(processConfiguration.getProcessID());
    }

    @Test
    public void testPauseUserSession() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration(mContext, mResultReceiver);
        Bundle bundle = new Bundle();
        processConfiguration.toBundle(bundle);
        mMetricaCore.pauseUserSession(bundle);

        verify(mApplicationStateProvider).pauseUserSessionForPid(processConfiguration.getProcessID());
    }

    @Test
    public void testPauseUserSessionForEmptyBundle() {
        testPauseUserSessionForInvalidBundle(new Bundle());
    }

    @Test
    public void testPauseUserSessionForBundleWithoutProcessConfiguration() {
        Bundle bundle = new Bundle();
        bundle.putString("Some bundle key", "Some bundle value");
        testPauseUserSessionForInvalidBundle(bundle);
    }

    @Test
    public void testPauseUserSessionForProcessConfigurationWithoutProcessId() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration(new ContentValues(), mResultReceiver);
        Bundle bundle = new Bundle();
        processConfiguration.toBundle(bundle);
        testPauseUserSessionForInvalidBundle(bundle);
    }

    private void testPauseUserSessionForInvalidBundle(Bundle bundle) {
        mMetricaCore.pauseUserSession(bundle);

        verifyNoMoreInteractions(mApplicationStateProvider);
    }

    @Test
    public void onUnbindNotifyApplicationStateProvider() {
        int pid = 3242;
        Intent intent = prepareMetricaIntent("io.appmetrica.analytics.IAppMetricaService", "client", pid, UUID.randomUUID().toString());
        mMetricaCore.onUnbind(intent);
        verify(mApplicationStateProvider).notifyProcessDisconnected(pid);
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForMissingAction() {
        testOnUnbindDoNotNotifyApplicationStateProvider(null, "client", 1123, UUID.randomUUID().toString());
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForEmptyAction() {
        testOnUnbindDoNotNotifyApplicationStateProvider("", "client", 1123, UUID.randomUUID().toString());
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForWrongAction() {
        testOnUnbindDoNotNotifyApplicationStateProvider("wrong_action", "client", 1123, UUID.randomUUID().toString());
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForMissingPath() {
        testOnUnbindDoNotNotifyApplicationStateProvider(
                "io.appmetrica.analytics.IAppMetricaService",
                null,
                1123,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForEmptyPath() {
        testOnUnbindDoNotNotifyApplicationStateProvider(
                "io.appmetrica.analytics.IAppMetricaService",
                "",
                1123,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void testOnUnbindDoNotNotifyApplicationStateProviderForInvalidPath() {
        testOnUnbindDoNotNotifyApplicationStateProvider(
                "io.appmetrica.analytics.IAppMetricaService",
                "invalid_path",
                1123,
                UUID.randomUUID().toString()
        );
    }

    @Test(expected = NumberFormatException.class)
    public void testOnUnbindDoNotNotifyApplicationStateProviderForMissingPid() {
        testOnUnbindDoNotNotifyApplicationStateProvider(
                "io.appmetrica.analytics.IAppMetricaService",
                "client",
                null,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void onDestroy() {
        mMetricaCore.onDestroy();
        verify(GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager()).onDestroy();
    }

    @Test
    public void reportData() {
        final int type = 2213;
        final Bundle data = mock(Bundle.class);
        mMetricaCore.reportData(type, data);
        verify(reportProxyMockedConstructionRule.getConstructionMock().constructed().get(0))
                .proxyReport(type, data);
    }

    private void testOnUnbindDoNotNotifyApplicationStateProvider(String action, String path, Integer pid, String psid) {
        Intent intent = prepareMetricaIntent(action, path, pid, psid);
        mMetricaCore.onUnbind(intent);
        verifyNoMoreInteractions(mApplicationStateProvider);
    }

    private Intent prepareMetricaIntent(String action, String path, Integer pid, String psid) {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedAuthority("unit.test.com");
        if (path != null) {
            builder.appendPath(path);
        }
        if (pid != null) {
            builder.appendQueryParameter("pid", String.valueOf(pid));
        }
        if (psid != null) {
            builder.appendQueryParameter("psid", psid);
        }
        Intent intent = new Intent();
        if (action != null) {
            intent.setAction(action);
        }
        intent.setData(builder.build());
        return intent;
    }

    private void touchNewClientConnectedObserver() {
        verify(mAppMetricaServiceLifecycle).addNewClientConnectObserver(mLifecycleObserverCaptor.capture());
        mLifecycleObserverCaptor.getValue().onEvent(intent);
    }
}
