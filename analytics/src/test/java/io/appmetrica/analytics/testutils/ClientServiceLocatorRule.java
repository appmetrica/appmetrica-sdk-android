package io.appmetrica.analytics.testutils;

import android.content.Context;
import android.os.Handler;
import io.appmetrica.analytics.impl.ActivityAppearedListener;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AnonymousClientActivator;
import io.appmetrica.analytics.impl.AppMetricaCoreComponentsProvider;
import io.appmetrica.analytics.impl.AppMetricaServiceDelayHandler;
import io.appmetrica.analytics.impl.ClientConfigSerializer;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.ContextAppearedListener;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.ScreenInfoRetriever;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.client.connection.ServiceDescriptionProvider;
import io.appmetrica.analytics.impl.crash.jvm.client.TechnicalCrashProcessorFactory;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.ClientStorageFactory;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.client.ClientModulesController;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.utils.AppMetricaServiceProcessDetector;
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector;
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.impl.utils.process.ProcessNameProvider;
import org.junit.rules.ExternalResource;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientServiceLocatorRule extends ExternalResource {

    public ClientExecutorProvider clientExecutorProvider;
    public DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    public CurrentProcessDetector currentProcessDetector;
    public ProcessNameProvider processNameProvider;
    public AppMetricaServiceProcessDetector appMetricaServiceProcessDetector;
    public AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;
    public SessionsTrackingManager sessionsTrackingManager;
    public ActivityLifecycleManager activityLifecycleManager;
    public ContextAppearedListener contextAppearedListener;
    public ActivityAppearedListener activityAppearedListener;
    public ScreenInfoRetriever screenInfoRetriever;
    public TechnicalCrashProcessorFactory crashProcessorFactory;
    public MultiProcessSafeUuidProvider defaultMultiProcessSafeUuidProvider;
    public MultiProcessSafeUuidProvider multiProcessSafeUuidProviderWithOuterSourceImporter;
    public ClientModulesController modulesController;
    public ModuleEntryPointsRegister moduleEntryPointsRegister;
    public PreferencesClientDbStorage preferencesClientDbStorage;
    public AppMetricaFacadeProvider appMetricaFacadeProvider;
    public AppMetricaCoreComponentsProvider appMetricaCoreComponentsProvider;
    public FirstLaunchDetector firstLaunchDetector;
    public ClientConfigSerializer clientConfigSerializer;
    public AnonymousClientActivator anonymousClientActivator;
    public ClientServiceLocator instance;
    public ReporterLifecycleListener reporterLifecycleListener;
    public ExtraMetaInfoRetriever extraMetaInfoRetriever;
    public ServiceDescriptionProvider serviceDescriptionProvider;
    public ClientStorageFactory clientStorageFactory;

    @Override
    public void before() {
        clientExecutorProvider = mock(ClientExecutorProvider.class);
        mDefaultOneShotMetricaConfig = mock(DefaultOneShotMetricaConfig.class);
        currentProcessDetector =  mock(CurrentProcessDetector.class);
        processNameProvider = mock(ProcessNameProvider.class);
        appMetricaServiceProcessDetector = mock(AppMetricaServiceProcessDetector.class);
        instance = mock(ClientServiceLocator.class);
        appMetricaServiceDelayHandler = mock(AppMetricaServiceDelayHandler.class);
        sessionsTrackingManager = mock(SessionsTrackingManager.class);
        activityLifecycleManager = mock(ActivityLifecycleManager.class);
        contextAppearedListener = mock(ContextAppearedListener.class);
        activityAppearedListener = mock(ActivityAppearedListener.class);
        screenInfoRetriever = mock(ScreenInfoRetriever.class);
        crashProcessorFactory = mock(TechnicalCrashProcessorFactory.class);
        defaultMultiProcessSafeUuidProvider = mock(MultiProcessSafeUuidProvider.class);
        multiProcessSafeUuidProviderWithOuterSourceImporter = mock(MultiProcessSafeUuidProvider.class);
        modulesController = mock(ClientModulesController.class);
        moduleEntryPointsRegister = mock(ModuleEntryPointsRegister.class);
        preferencesClientDbStorage = mock(PreferencesClientDbStorage.class);
        appMetricaFacadeProvider = mock(AppMetricaFacadeProvider.class);
        appMetricaCoreComponentsProvider = mock(AppMetricaCoreComponentsProvider.class);
        firstLaunchDetector = mock(FirstLaunchDetector.class);
        reporterLifecycleListener = mock(ReporterLifecycleListener.class);
        clientConfigSerializer = mock(ClientConfigSerializer.class);
        anonymousClientActivator = mock(AnonymousClientActivator.class);
        extraMetaInfoRetriever = mock(ExtraMetaInfoRetriever.class);
        serviceDescriptionProvider = mock(ServiceDescriptionProvider.class);
        clientStorageFactory = mock(ClientStorageFactory.class);
        when(instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(instance.getDefaultOneShotConfig()).thenReturn(mDefaultOneShotMetricaConfig);
        when(instance.getCurrentProcessDetector()).thenReturn(currentProcessDetector);
        when(instance.getProcessNameProvider()).thenReturn(processNameProvider);
        when(instance.getAppMetricaServiceProcessDetector()).thenReturn(appMetricaServiceProcessDetector);
        when(instance.getAppMetricaServiceDelayHandler()).thenReturn(appMetricaServiceDelayHandler);
        when(instance.getActivityLifecycleManager()).thenReturn(activityLifecycleManager);
        when(instance.getSessionsTrackingManager()).thenReturn(sessionsTrackingManager);
        when(instance.getContextAppearedListener()).thenReturn(contextAppearedListener);
        when(instance.getActivityAppearedListener()).thenReturn(activityAppearedListener);
        when(instance.getScreenInfoRetriever()).thenReturn(screenInfoRetriever);
        when(instance.getCrashProcessorFactory()).thenReturn(crashProcessorFactory);
        when(instance.getMultiProcessSafeUuidProvider(ArgumentMatchers.<Context>any()))
            .thenReturn(defaultMultiProcessSafeUuidProvider);
        when(instance.getModulesController()).thenReturn(modulesController);
        when(instance.getModuleEntryPointsRegister()).thenReturn(moduleEntryPointsRegister);
        when(instance.getPreferencesClientDbStorage(any())).thenReturn(preferencesClientDbStorage);
        when(instance.getAppMetricaFacadeProvider()).thenReturn(appMetricaFacadeProvider);
        when(instance.getAppMetricaCoreComponentsProvider()).thenReturn(appMetricaCoreComponentsProvider);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(new StubbedBlockingExecutor());
        when(clientExecutorProvider.getReportSenderExecutor()).thenReturn(new StubbedBlockingExecutor());
        Handler mainHandler = TestUtils.createBlockingExecutionHandlerStub();
        when(clientExecutorProvider.getMainHandler()).thenReturn(mainHandler);
        when(instance.getFirstLaunchDetector()).thenReturn(firstLaunchDetector);
        when(instance.getAnonymousClientActivator()).thenReturn(anonymousClientActivator);
        when(instance.getReporterLifecycleListener()).thenReturn(reporterLifecycleListener);
        when(instance.getExtraMetaInfoRetriever(any())).thenReturn(extraMetaInfoRetriever);
        when(instance.getServiceDescriptionProvider()).thenReturn(serviceDescriptionProvider);
        when(instance.getStorageFactory(any())).thenReturn(clientStorageFactory);
        ClientServiceLocator.setInstance(instance);
    }

    @Override
    public void after() {
        ClientServiceLocator.setInstance(null);
    }

}
