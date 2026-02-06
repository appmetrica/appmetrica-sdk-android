package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceProvider;
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.service.ServiceCrashController;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.modules.ModuleServiceLifecycleControllerImpl;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

public class AppMetricaServiceCoreImplOnCreateTest extends CommonTest {

    private Context mContext;
    @Mock
    private AppMetricaServiceCallback mCallback;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @Mock
    private FirstServiceEntryPointManager firstServiceEntryPointManager;
    @Mock
    private ApplicationStateProviderImpl applicationStateProvider;
    @Mock
    private AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
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
            (mock, context) -> when(mock.getLauncher()).thenReturn(mock(CoreImplFirstCreateTaskLauncher.class)));

    @Rule
    public MockedConstructionRule<ServiceCrashController> serviceCrashControllerMockedConstructionRule =
        new MockedConstructionRule<>(ServiceCrashController.class);

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Before
    public void setUp() {
        mContext = contextRule.getContext();
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
            .thenReturn(mock(VitalCommonDataProvider.class));
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
            fieldsFactory
        );
    }

    @Test
    public void onCreateTwice() {
        try (MockedStatic<AppMetricaSelfReportFacade> appMetricaSelfFacadeStaticMock = Mockito.mockStatic(AppMetricaSelfReportFacade.class)) {
            when(AppMetricaSelfReportFacade.getReporter()).thenReturn(mock(SelfReporterWrapper.class));
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
            appMetricaSelfFacadeStaticMock.verify(() -> AppMetricaSelfReportFacade.warmupForSelfProcess(mContext));
            verify(globalServiceLocator.getLifecycleDependentComponentManager()).onCreate();
            verify(serviceCrashController()).init();

            mMetricaCore.onDestroy();
            clearInvocations(activationBarrier, firstServiceEntryPointManager, globalServiceLocator, mAppMetricaServiceLifecycle,
                advertisingIdGetter, fieldsFactory, globalServiceLocator.getNativeCrashService(),
                globalServiceLocator.getLifecycleDependentComponentManager());
            mMetricaCore.onCreate();

            verify(firstServiceEntryPointManager, never()).onPossibleFirstEntry(mContext);
            verify(globalServiceLocator, never()).initAsync();
            verifyNoMoreInteractions(mAppMetricaServiceLifecycle);
            verify(advertisingIdGetter, never()).init();
            verify(fieldsFactory, never()).createReportConsumer(same(mContext), any(ClientRepository.class));
            appMetricaSelfFacadeStaticMock.verify(() -> AppMetricaSelfReportFacade.warmupForSelfProcess(mContext));
            verify(globalServiceLocator.getNativeCrashService(), never()).initNativeCrashReporting(any(Context.class), any(ReportConsumer.class));
            verify(globalServiceLocator.getLifecycleDependentComponentManager()).onCreate();
            assertThat(serviceCrashControllerMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
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

    private ServiceCrashController serviceCrashController() {
        assertThat(serviceCrashControllerMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(serviceCrashControllerMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(mContext, reportConsumer);
        return serviceCrashControllerMockedConstructionRule.getConstructionMock().constructed().get(0);
    }
}
