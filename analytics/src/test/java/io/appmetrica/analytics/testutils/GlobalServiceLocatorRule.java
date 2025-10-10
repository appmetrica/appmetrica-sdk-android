package io.appmetrica.analytics.testutils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.coreutils.internal.ReferenceHolder;
import io.appmetrica.analytics.coreutils.internal.services.FirstExecutionConditionServiceImpl;
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier;
import io.appmetrica.analytics.impl.ApplicationStateProviderImpl;
import io.appmetrica.analytics.impl.BatteryInfoProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.SdkEnvironmentHolder;
import io.appmetrica.analytics.impl.SelfDiagnosticReporterStorage;
import io.appmetrica.analytics.impl.StartupStateHolder;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ServiceModuleReporterComponentLifecycleImpl;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.VitalDataProviderStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.id.AppSetIdGetter;
import io.appmetrica.analytics.impl.location.LocationApi;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder;
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController;
import io.appmetrica.analytics.impl.network.http.SslSocketFactoryProviderImpl;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder;
import io.appmetrica.analytics.impl.servicecomponents.ServiceLifecycleTimeTracker;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider;
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ExecutorWrapper;
import io.appmetrica.analytics.impl.utils.executors.InterruptionSafeHandlerThread;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;
import io.appmetrica.analytics.networktasks.internal.NetworkCore;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.junit.rules.ExternalResource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalServiceLocatorRule extends ExternalResource {

    private static Answer sRunnableAnswer = new Answer() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }
    };
    private static StubbedExecutorWrapper sStubbedExecutor = new StubbedExecutorWrapper("Stubbed executor thread");
    private static Answer sStubbedThreadAnswer = new Answer() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            return new InterruptionSafeThread(((Runnable) invocation.getArgument(0)));
        }
    };

    @Override
    public void before() {
        GlobalServiceLocator globalServiceLocator = mock(GlobalServiceLocator.class);
        ServiceExecutorProvider serviceExecutorProvider = createStubbedServiceThreadProvider();
        when(globalServiceLocator.getServiceExecutorProvider()).thenReturn(serviceExecutorProvider);
        when(globalServiceLocator.getServicePreferences()).thenReturn(mock(PreferencesServiceDbStorage.class));
        LifecycleDependentComponentManager lifecycleDependentComponentManager = mock(LifecycleDependentComponentManager.class);
        ApplicationStateProviderImpl applicationStateProvider = mock(ApplicationStateProviderImpl.class);
        when(applicationStateProvider.getCurrentState()).thenReturn(ApplicationState.UNKNOWN);
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class))).thenReturn(ApplicationState.UNKNOWN);
        when(lifecycleDependentComponentManager.getApplicationStateProvider()).thenReturn(applicationStateProvider);
        when(globalServiceLocator.getApplicationStateProvider()).thenReturn(applicationStateProvider);
        when(globalServiceLocator.getLifecycleDependentComponentManager()).thenReturn(lifecycleDependentComponentManager);
        when(globalServiceLocator.getBatteryInfoProvider()).thenReturn(mock(BatteryInfoProvider.class));
        when(globalServiceLocator.getClidsStorage()).thenReturn(mock(ClidsInfoStorage.class));
        Context context = TestUtils.createMockedContext();
        when(globalServiceLocator.getContext()).thenReturn(context);
        when(globalServiceLocator.getPreloadInfoStorage()).thenReturn(mock(PreloadInfoStorage.class));
        when(globalServiceLocator.getReferrerHolder()).thenReturn(mock(ReferrerHolder.class));
        when(globalServiceLocator.getSelfDiagnosticReporterStorage()).thenReturn(mock(SelfDiagnosticReporterStorage.class));
        when(globalServiceLocator.getDataSendingRestrictionController()).thenReturn(mock(DataSendingRestrictionControllerImpl.class));
        when(globalServiceLocator.getSslSocketFactoryProvider()).thenReturn(mock(SslSocketFactoryProviderImpl.class));
        VitalDataProviderStorage vitalDataProviderStorage = mock(VitalDataProviderStorage.class);
        when(vitalDataProviderStorage.getCommonDataProvider()).thenReturn(mock(VitalCommonDataProvider.class));
        when(vitalDataProviderStorage.getCommonDataProviderForMigration()).thenReturn(mock(VitalCommonDataProvider.class));
        when(vitalDataProviderStorage.getComponentDataProvider(any(ComponentId.class))).thenReturn(mock(VitalComponentDataProvider.class));
        when(globalServiceLocator.getVitalDataProviderStorage()).thenReturn(vitalDataProviderStorage);
        when(globalServiceLocator.getModulesController()).thenReturn(mock(ServiceModulesController.class));
        when(globalServiceLocator.getGeneralPermissionExtractor()).thenReturn(mock(PermissionExtractor.class));
        StartupStateHolder startupStateHolder = mock(StartupStateHolder.class);
        when(startupStateHolder.getStartupState())
            .thenReturn(new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).build());
        when(globalServiceLocator.getStartupStateHolder()).thenReturn(startupStateHolder);
        LocationApi locationApi = mock(LocationApi.class);
        when(globalServiceLocator.getLocationClientApi()).thenReturn(locationApi);
        when(globalServiceLocator.getLocationServiceApi()).thenReturn(locationApi);
        when(globalServiceLocator.getServiceDataReporterHolder()).thenReturn(mock(ServiceDataReporterHolder.class));
        when(globalServiceLocator.getModuleEventHandlersHolder()).thenReturn(mock(ModuleEventHandlersHolder.class));
        TelephonyDataProvider telephonyDataProvider = mock(TelephonyDataProvider.class);
        when(globalServiceLocator.getTelephonyDataProvider()).thenReturn(telephonyDataProvider);
        when(globalServiceLocator.getModuleEntryPointsRegister()).thenReturn(mock(ModuleEntryPointsRegister.class));
        when(globalServiceLocator.getMultiProcessSafeUuidProvider()).thenReturn(mock(MultiProcessSafeUuidProvider.class));
        when(globalServiceLocator.getNativeCrashService()).thenReturn(mock(NativeCrashService.class));
        when(globalServiceLocator.getServicePreferences()).thenReturn(mock(PreferencesServiceDbStorage.class));
        when(globalServiceLocator.getNetworkCore()).thenReturn(mock(NetworkCore.class));
        when(globalServiceLocator.getSdkEnvironmentHolder()).thenReturn(mock(SdkEnvironmentHolder.class));
        AppSetIdGetter appSetIdGetter = mock(AppSetIdGetter.class);
        when(appSetIdGetter.getAppSetId()).thenReturn(new AppSetId(UUID.randomUUID().toString(), AppSetIdScope.APP));
        AdvertisingIdGetter advertisingIdGetter = mock(AdvertisingIdGetter.class);
        PlatformIdentifiers platformIdentifiers = mock(PlatformIdentifiers.class);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdGetter);
        when(globalServiceLocator.getAppSetIdGetter()).thenReturn(appSetIdGetter);
        when(globalServiceLocator.getAdvertisingIdGetter()).thenReturn(advertisingIdGetter);
        when(globalServiceLocator.getPlatformIdentifiers()).thenReturn(platformIdentifiers);
        when(globalServiceLocator.getActivationBarrier()).thenReturn(mock(WaitForActivationDelayBarrier.class));
        when(globalServiceLocator.getFirstExecutionConditionService())
            .thenReturn(mock(FirstExecutionConditionServiceImpl.class));
        when(globalServiceLocator.getExtraMetaInfoRetriever()).thenReturn(mock(ExtraMetaInfoRetriever.class));
        when(globalServiceLocator.getServiceLifecycleTimeTracker()).thenReturn(mock(ServiceLifecycleTimeTracker.class));
        when(globalServiceLocator.getCurrentProcessDetector()).thenReturn(mock(CurrentProcessDetector.class));
        when(globalServiceLocator.getReferenceHolder()).thenReturn(mock(ReferenceHolder.class));
        when(globalServiceLocator.getServiceModuleReporterComponentLifecycle())
            .thenReturn(mock(ServiceModuleReporterComponentLifecycleImpl.class));
        when(globalServiceLocator.getActiveNetworkTypeProvider()).thenReturn(mock(ActiveNetworkTypeProvider.class));
        GlobalServiceLocator.setInstance(globalServiceLocator);
    }

    @Override
    public void after() {
        GlobalServiceLocator.destroy();
    }

    private ServiceExecutorProvider createStubbedServiceThreadProvider() {
        ServiceExecutorProvider serviceExecutorProvider = mock(ServiceExecutorProvider.class);
        when(serviceExecutorProvider.getReportRunnableExecutor()).thenReturn(sStubbedExecutor);
        IHandlerExecutor moduleExecutor = createMockedExecutor();
        when(serviceExecutorProvider.getModuleExecutor()).thenReturn(moduleExecutor);
        when(serviceExecutorProvider.getNetworkTaskProcessorExecutor()).thenReturn(sStubbedExecutor);
        when(serviceExecutorProvider.getSupportIOExecutor()).thenReturn(sStubbedExecutor);
        when(serviceExecutorProvider.getDefaultExecutor()).thenReturn(sStubbedExecutor);
        when(serviceExecutorProvider.getUiExecutor()).thenReturn(sStubbedExecutor);
        when(serviceExecutorProvider.getMetricaCoreExecutor()).thenReturn(sStubbedExecutor);
        Executor blockingExecutorStub = mock(Executor.class);
        doAnswer(sRunnableAnswer).when(blockingExecutorStub).execute(any(Runnable.class));
        return serviceExecutorProvider;
    }

    private IHandlerExecutor createMockedExecutor() {
        IHandlerExecutor mock = mock(IHandlerExecutor.class);
        Answer<Void> blockingAnswer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        };
        doAnswer(blockingAnswer).when(mock).execute(any(Runnable.class));
        doAnswer(blockingAnswer).when(mock).executeDelayed(any(Runnable.class), anyLong());
        doAnswer(blockingAnswer).when(mock).executeDelayed(any(Runnable.class), anyLong(), any(TimeUnit.class));
        Handler handlerMock = mock(Handler.class);
        when(mock.getHandler()).thenReturn(handlerMock);
        Looper looperMock = mock(Looper.class);
        when(mock.getLooper()).thenReturn(looperMock);
        return mock;
    }

    static class StubbedExecutorWrapper extends ExecutorWrapper {

        public StubbedExecutorWrapper(@NonNull String threadName) {
            super(threadName);
        }

        public StubbedExecutorWrapper(@NonNull InterruptionSafeHandlerThread handlerThread,
                                      @NonNull Looper looper,
                                      @NonNull Handler handler) {
            super(handlerThread, looper, handler);
        }

        @NonNull
        @Override
        public Handler getHandler() {
            Handler handler = mock(Handler.class);
            doAnswer(sRunnableAnswer).when(handler).post(any(Runnable.class));
            doAnswer(sRunnableAnswer).when(handler).postDelayed(any(Runnable.class), anyLong());
            return handler;
        }

        @NonNull
        @Override
        public Looper getLooper() {
            return mock(Looper.class);
        }

        @Override
        public void execute(@NonNull Runnable runnable) {
            runnable.run();
        }

        @Override
        public <T> Future<T> submit(Callable<T> callable) {
            FutureTask<T> futureTask = new FutureTask<T>(callable);
            futureTask.run();
            return futureTask;
        }

        @Override
        public void executeDelayed(@NonNull Runnable runnable, long delay) {
            runnable.run();
        }

        @Override
        public void executeDelayed(@NonNull Runnable runnable, long delay, @NonNull TimeUnit timeUnit) {
            runnable.run();
        }

        @Override
        public void remove(@NonNull Runnable runnable) {
            //Do nothing
        }
    }
}
