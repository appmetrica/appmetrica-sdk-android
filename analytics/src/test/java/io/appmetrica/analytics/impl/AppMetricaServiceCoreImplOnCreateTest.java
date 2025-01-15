package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceProvider;
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.ReadOldCrashesRunnable;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.modules.ModuleServiceLifecycleControllerImpl;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreImplOnCreateTest extends CommonTest {

    private Context mContext;
    @Mock
    private AppMetricaServiceCallback mCallback;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
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
    private WaitForActivationDelayBarrier activationBarrier;

    private AppMetricaServiceCoreImpl mMetricaCore;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();
    @Rule
    public MockedConstructionRule<UtilityServiceProvider> utilitiesMockedConstructionRule =
        new MockedConstructionRule<>(UtilityServiceProvider.class);
    @Rule
    public MockedConstructionRule<ServiceContextFacade> serviceContextFacadeMockedRule =
        new MockedConstructionRule<>(ServiceContextFacade.class);
    @Rule
    public MockedConstructionRule<ModuleServiceLifecycleControllerImpl> moduleLifecycleControllerMockedRule =
        new MockedConstructionRule<>(ModuleServiceLifecycleControllerImpl.class);

    @Rule
    public MockedConstructionRule<CoreImplFirstCreateTaskLauncherProvider>
        firstCreateTaskLauncherProviderMockedConstructionRule =
        new MockedConstructionRule<>(
            CoreImplFirstCreateTaskLauncherProvider.class,
            new MockedConstruction.MockInitializer<CoreImplFirstCreateTaskLauncherProvider>() {
                @Override
                public void prepare(CoreImplFirstCreateTaskLauncherProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getLauncher()).thenReturn(mock(CoreImplFirstCreateTaskLauncher.class));
                }
            });

    @Rule
    public MockedStaticRule<FileUtils> fileUtilsMockedRule = new MockedStaticRule<>(FileUtils.class);

    @Before
    public void setUp() {
        mContext = TestUtils.createMockedContext();
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
            .thenReturn(mock(VitalCommonDataProvider.class));
        when(FileUtils.getCrashesDirectory(mContext)).thenReturn(mCrashesDirectory);
        doReturn(crashDirectoryWatcher).when(fieldsFactory).createCrashDirectoryWatcher(
            any(File.class),
            any(Consumer.class)
        );
        doReturn(reportConsumer).when(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));
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
        when(GlobalServiceLocator.getInstance().getAdvertisingIdGetter()).thenReturn(advertisingIdGetter);
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verify(advertisingIdGetter).init();
    }

    @Test
    public void doNotSubscribeToCrashesIfDirectoryIsNull() {
        when(FileUtils.getCrashesDirectory(mContext)).thenReturn(null);
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        verifyNoMoreInteractions(reportExecutor, crashDirectoryWatcher);
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
        verifyNoMoreInteractions(crashDirectoryWatcher);
    }

    @Test
    public void onFirstCreateLaunchOnFirstCreateTasks() {
        initMetricaCoreImpl();
        mMetricaCore.onCreate();
        CoreImplFirstCreateTaskLauncher launcher = firstCreateTaskLauncherProvider().getLauncher();
        verify(launcher).run();
        clearInvocations(launcher);
        mMetricaCore.onCreate();
        verifyNoInteractions(launcher);
    }

    private void initMetricaCoreImpl() {
        mMetricaCore = new AppMetricaServiceCoreImpl(
            mContext,
            mCallback,
            mClientRepository,
            mAppMetricaServiceLifecycle,
            firstServiceEntryPointManager,
            applicationStateProvider,
            reportExecutor,
            fieldsFactory
        );
    }

    @Test
    public void onCreateTwice() {
        try (MockedStatic<AppMetricaSelfReportFacade> ignored = Mockito.mockStatic(AppMetricaSelfReportFacade.class)) {
            when(AppMetricaSelfReportFacade.getReporter()).thenReturn(mock(IReporterExtended.class));
            initMetricaCoreImpl();
            mMetricaCore.onCreate();
            GlobalServiceLocator globalServiceLocator = GlobalServiceLocator.getInstance();
            AdvertisingIdGetter advertisingIdGetter = globalServiceLocator.getAdvertisingIdGetter();
            verify(firstServiceEntryPointManager).onPossibleFirstEntry(mContext);
            verify(globalServiceLocator).initAsync();
            verify(mAppMetricaServiceLifecycle).addNewClientConnectObserver(any(AppMetricaServiceLifecycle.LifecycleObserver.class));
            verify(advertisingIdGetter).init();
            verify(GlobalServiceLocator.getInstance().getStartupStateHolder()).registerObserver(GlobalServiceLocator.getInstance().getModulesController());
            verify(GlobalServiceLocator.getInstance().getStartupStateHolder()).registerObserver(any(StartupStateObserver.class));
            verify(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));
            verify(reportExecutor).execute(any(Runnable.class));
            verify(crashDirectoryWatcher).startWatching();
            verify(globalServiceLocator.getNativeCrashService()).initNativeCrashReporting(mContext, reportConsumer);
            verify(globalServiceLocator.getLifecycleDependentComponentManager()).onCreate();

            mMetricaCore.onDestroy();
            clearInvocations(activationBarrier, firstServiceEntryPointManager, globalServiceLocator, mAppMetricaServiceLifecycle,
                advertisingIdGetter, fieldsFactory, reportExecutor, crashDirectoryWatcher, globalServiceLocator.getNativeCrashService(),
                globalServiceLocator.getLifecycleDependentComponentManager());
            mMetricaCore.onCreate();

            verify(firstServiceEntryPointManager, never()).onPossibleFirstEntry(mContext);
            verify(globalServiceLocator, never()).initAsync();
            verifyNoMoreInteractions(mAppMetricaServiceLifecycle);
            verify(advertisingIdGetter, never()).init();
            verify(fieldsFactory, never()).createReportConsumer(same(mContext), any(ClientRepository.class));
            verify(reportExecutor, never()).execute(any(Runnable.class));
            verify(crashDirectoryWatcher, never()).startWatching();
            verify(globalServiceLocator.getNativeCrashService(), never()).initNativeCrashReporting(any(Context.class), any(ReportConsumer.class));
            verify(globalServiceLocator.getLifecycleDependentComponentManager()).onCreate();
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

    private CoreImplFirstCreateTaskLauncherProvider firstCreateTaskLauncherProvider() {
        assertThat(firstCreateTaskLauncherProviderMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(firstCreateTaskLauncherProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
        return firstCreateTaskLauncherProviderMockedConstructionRule.getConstructionMock().constructed().get(0);
    }
}
