package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.services.ActivationBarrier;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.MetricaCoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.MetricaCoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.ReadOldCrashesRunnable;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.modules.ModuleLifecycleControllerImpl;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.service.MetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.TestUtils;
import io.appmetrica.analytics.testutils.rules.coreutils.UtilityServiceLocatorRule;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppAppMetricaServiceCoreImplOnCreateTest extends CommonTest {

    private Context mContext;
    @Mock
    private MetricaServiceCallback mCallback;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @Mock
    private FileProvider mFileProvider;
    @Mock
    private File mCrashesDirectory;
    @Mock
    private FirstServiceEntryPointManager firstServiceEntryPointManager;
    @Mock
    private ApplicationStateProviderImpl applicationStateProvider;
    @Mock
    private AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
    @Mock
    private CrashDirectoryWatcher crashDirectoryWatcher;
    @Mock
    private ICommonExecutor reportExecutor;
    @Mock
    private ReportConsumer reportConsumer;
    @Mock
    private ActivationBarrier activationBarrier;
    @Mock
    private ScreenInfoHolder screenInfoHolder;

    private AppAppMetricaServiceCoreImpl mMetricaCore;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();
    @Rule
    public UtilityServiceLocatorRule utilityServiceLocatorMockingRule = new UtilityServiceLocatorRule();
    @Rule
    public MockedConstructionRule<ServiceContextFacade> serviceContextFacadeMockedRule =
        new MockedConstructionRule<>(ServiceContextFacade.class);
    @Rule
    public MockedConstructionRule<ModuleLifecycleControllerImpl> moduleLifecycleControllerMockedRule =
        new MockedConstructionRule<>(ModuleLifecycleControllerImpl.class);

    @Rule
    public MockedConstructionRule<MetricaCoreImplFirstCreateTaskLauncherProvider>
        firstCreateTaskLauncherProviderMockedConstructionRule =
        new MockedConstructionRule<>(
            MetricaCoreImplFirstCreateTaskLauncherProvider.class,
            new MockedConstruction.MockInitializer<MetricaCoreImplFirstCreateTaskLauncherProvider>() {
                @Override
                public void prepare(MetricaCoreImplFirstCreateTaskLauncherProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getLauncher()).thenReturn(mock(MetricaCoreImplFirstCreateTaskLauncher.class));
                }
            });

    @Before
    public void setUp() {
        mContext = TestUtils.createMockedContext();
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
            .thenReturn(mock(VitalCommonDataProvider.class));
        when(mFileProvider.getCrashesDirectory(mContext)).thenReturn(mCrashesDirectory);
        doReturn(crashDirectoryWatcher).when(fieldsFactory).createCrashDirectoryWatcher(
            any(File.class),
            any(Consumer.class)
        );
        doReturn(reportConsumer).when(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));
    }

    @Test
    public void testOnInitializationFinished() {
        initMetricaCoreImpl();
        try (MockedStatic<AppMetricaSelfReportFacade> staticMock = Mockito.mockStatic(AppMetricaSelfReportFacade.class)) {
            when(AppMetricaSelfReportFacade.getReporter()).thenReturn(mock(IReporterExtended.class));
            mMetricaCore.onCreate();
            staticMock.verify(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    AppMetricaSelfReportFacade.warmupForMetricaProcess(mContext);
                }
            });
        }
    }

    @Test
    public void onPossibleFirstEntry() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verify(firstServiceEntryPointManager).onPossibleFirstEntry(mContext);
    }

    @Test
    public void urlConnectionFactoryIsInited() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verify(GlobalServiceLocator.getInstance().getSslSocketFactoryProvider())
            .onStartupStateChanged(any(StartupState.class));
    }

    @Test
    public void testAdvertisingIdGetterIsInited() {
        AdvertisingIdGetter advertisingIdGetter = mock(AdvertisingIdGetter.class);
        when(GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter()).thenReturn(advertisingIdGetter);
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verify(advertisingIdGetter).init(same(mContext), any(StartupState.class));
    }

    @Test
    public void doNotSubscribeToCrashesIfDirectoryIsNull() {
        when(mFileProvider.getCrashesDirectory(mContext)).thenReturn(null);
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verifyZeroInteractions(reportExecutor, crashDirectoryWatcher);
    }

    @Test
    public void subscribeToCrashes() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();

        ArgumentCaptor<Consumer<File>> listenerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(fieldsFactory).createCrashDirectoryWatcher(
            same(mCrashesDirectory),
            listenerCaptor.capture()
        );
        Consumer<File> listener = listenerCaptor.getValue();

        File file = mock(File.class);
        ReportConsumer reportConsumer = mock(ReportConsumer.class);
        mMetricaCore.setReportConsumer(reportConsumer);
        listener.consume(file);
        verify(reportConsumer).consumeCrashFromFile(file);

        ArgumentCaptor<ReadOldCrashesRunnable> captor = ArgumentCaptor.forClass(ReadOldCrashesRunnable.class);
        verify(reportExecutor).execute(captor.capture());

        assertThat(captor.getValue()).extracting("crashDirectory").isSameAs(mCrashesDirectory);
        assertThat(captor.getValue()).extracting("newCrashListener").isSameAs(listener);

        verify(crashDirectoryWatcher).startWatching();

        mMetricaCore.onDestroy();
        clearInvocations(fieldsFactory, crashDirectoryWatcher);
        mMetricaCore.onCreate();
        verify(fieldsFactory, never()).createCrashDirectoryWatcher(any(File.class), any(Consumer.class));
        verifyZeroInteractions(crashDirectoryWatcher);
    }

    @Test
    public void onFirstCreateLaunchOnFirstCreateTasks() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        MetricaCoreImplFirstCreateTaskLauncher launcher = firstCreateTaskLauncherProvider().getLauncher();
        verify(launcher).run();
        clearInvocations(launcher);
        mMetricaCore.onCreate();
        verifyNoInteractions(launcher);
    }

    private void initMetricaCoreImpl() {
        mMetricaCore = new AppAppMetricaServiceCoreImpl(
            mContext,
            mCallback,
            mClientRepository,
            mAppMetricaServiceLifecycle,
            mFileProvider,
            firstServiceEntryPointManager,
            applicationStateProvider,
            reportExecutor,
            fieldsFactory,
            screenInfoHolder
        );
    }

    @Test
    public void onCreateTwice() {
        try (MockedStatic<AppMetricaSelfReportFacade> ignored = Mockito.mockStatic(AppMetricaSelfReportFacade.class)) {
            when(AppMetricaSelfReportFacade.getReporter()).thenReturn(mock(IReporterExtended.class));
            initMetricaCoreImpl();
            mMetricaCore.onCreate();
            GlobalServiceLocator globalServiceLocator = GlobalServiceLocator.getInstance();
            AdvertisingIdGetter advertisingIdGetter = globalServiceLocator.getServiceInternalAdvertisingIdGetter();
            verify(firstServiceEntryPointManager).onPossibleFirstEntry(mContext);
            verify(globalServiceLocator).initAsync();
            verify(mAppMetricaServiceLifecycle).addNewClientConnectObserver(any(AppMetricaServiceLifecycle.LifecycleObserver.class));
            verify(advertisingIdGetter).init(same(mContext), any(StartupState.class));
            verify(GlobalServiceLocator.getInstance().getStartupStateHolder()).registerObserver(GlobalServiceLocator.getInstance().getModulesController());
            verify(GlobalServiceLocator.getInstance().getStartupStateHolder()).registerObserver(any(StartupStateObserver.class));
            verify(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));
            verify(AppMetricaSelfReportFacade.class);
            AppMetricaSelfReportFacade.warmupForMetricaProcess(mContext);
            verify(reportExecutor).execute(any(Runnable.class));
            verify(crashDirectoryWatcher).startWatching();
            verify(globalServiceLocator.getNativeCrashService()).initNativeCrashReporting(mContext, reportConsumer);

            mMetricaCore.onDestroy();
            clearInvocations(activationBarrier, firstServiceEntryPointManager, globalServiceLocator, mAppMetricaServiceLifecycle,
                advertisingIdGetter, fieldsFactory, reportExecutor, crashDirectoryWatcher, globalServiceLocator.getNativeCrashService());
            mMetricaCore.onCreate();

            verify(firstServiceEntryPointManager, never()).onPossibleFirstEntry(mContext);
            verify(globalServiceLocator, never()).initAsync();
            verifyZeroInteractions(mAppMetricaServiceLifecycle);
            verify(advertisingIdGetter, never()).init(same(mContext), any(StartupState.class));
            verify(fieldsFactory, never()).createReportConsumer(same(mContext), any(ClientRepository.class));
            verify(AppMetricaSelfReportFacade.class, times(1));
            AppMetricaSelfReportFacade.warmupForMetricaProcess(mContext);
            verify(reportExecutor, never()).execute(any(Runnable.class));
            verify(crashDirectoryWatcher, never()).startWatching();
            verify(globalServiceLocator.getNativeCrashService(), never()).initNativeCrashReporting(any(Context.class), any(ReportConsumer.class));
        }
    }

    @Test
    public void initModulesServiceSide() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        assertThat(moduleLifecycleControllerMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(moduleLifecycleControllerMockedRule.getArgumentInterceptor().flatArguments())
            .containsExactly(mAppMetricaServiceLifecycle);
        assertThat(serviceContextFacadeMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(serviceContextFacadeMockedRule.getArgumentInterceptor().flatArguments())
            .containsExactly(moduleLifecycleControllerMockedRule.getConstructionMock().constructed().get(0));
        verify(GlobalServiceLocator.getInstance().getModulesController())
            .initServiceSide(
                eq(serviceContextFacadeMockedRule.getConstructionMock().constructed().get(0)),
                any(StartupState.class)
            );
    }

    private MetricaCoreImplFirstCreateTaskLauncherProvider firstCreateTaskLauncherProvider() {
        assertThat(firstCreateTaskLauncherProviderMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(firstCreateTaskLauncherProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
        return firstCreateTaskLauncherProviderMockedConstructionRule.getConstructionMock().constructed().get(0);
    }
}
