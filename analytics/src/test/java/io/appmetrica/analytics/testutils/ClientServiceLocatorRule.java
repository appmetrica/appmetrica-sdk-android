package io.appmetrica.analytics.testutils;

import android.content.Context;
import io.appmetrica.analytics.impl.ActivityAppearedListener;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AppMetricaServiceDelayHandler;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.ContextAppearedListener;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.crash.CrashProcessorFactory;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.utils.MainProcessDetector;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import org.junit.rules.ExternalResource;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientServiceLocatorRule extends ExternalResource {

    public ClientExecutorProvider clientExecutorProvider;
    public DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    public MainProcessDetector mainProcessDetector;
    public ProcessDetector processDetector;
    public AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;
    public SessionsTrackingManager sessionsTrackingManager;
    public ActivityLifecycleManager activityLifecycleManager;
    public ContextAppearedListener contextAppearedListener;
    public ActivityAppearedListener activityAppearedListener;
    public CrashProcessorFactory crashProcessorFactory;
    public MultiProcessSafeUuidProvider defaultMultiProcessSafeUuidProvider;
    public MultiProcessSafeUuidProvider multiProcessSafeUuidProviderWithOuterSourceImporter;
    public ClientServiceLocator instance;

    @Override
    public void before() {
        clientExecutorProvider = new ClientExecutorProviderStub();
        mDefaultOneShotMetricaConfig = mock(DefaultOneShotMetricaConfig.class);
        mainProcessDetector =  mock(MainProcessDetector.class);
        processDetector = mock(ProcessDetector.class);
        instance = mock(ClientServiceLocator.class);
        appMetricaServiceDelayHandler = mock(AppMetricaServiceDelayHandler.class);
        sessionsTrackingManager = mock(SessionsTrackingManager.class);
        activityLifecycleManager = mock(ActivityLifecycleManager.class);
        contextAppearedListener = mock(ContextAppearedListener.class);
        activityAppearedListener = mock(ActivityAppearedListener.class);
        crashProcessorFactory = mock(CrashProcessorFactory.class);
        defaultMultiProcessSafeUuidProvider = mock(MultiProcessSafeUuidProvider.class);
        multiProcessSafeUuidProviderWithOuterSourceImporter = mock(MultiProcessSafeUuidProvider.class);
        when(instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(instance.getApiProxyExecutor()).thenReturn(clientExecutorProvider.getApiProxyExecutor());
        when(instance.getDefaultOneShotConfig()).thenReturn(mDefaultOneShotMetricaConfig);
        when(instance.getMainProcessDetector()).thenReturn(mainProcessDetector);
        when(instance.getProcessDetector()).thenReturn(processDetector);
        when(instance.getAppMetricaServiceDelayHandler()).thenReturn(appMetricaServiceDelayHandler);
        when(instance.getActivityLifecycleManager()).thenReturn(activityLifecycleManager);
        when(instance.getSessionsTrackingManager()).thenReturn(sessionsTrackingManager);
        when(instance.getContextAppearedListener()).thenReturn(contextAppearedListener);
        when(instance.getActivityAppearedListener()).thenReturn(activityAppearedListener);
        when(instance.getCrashProcessorFactory()).thenReturn(crashProcessorFactory);
        when(instance.getMultiProcessSafeUuidProvider(ArgumentMatchers.<Context>any()))
            .thenReturn(defaultMultiProcessSafeUuidProvider);
        ClientServiceLocator.setInstance(instance);
    }

    @Override
    public void after() {
        ClientServiceLocator.setInstance(null);
    }

}
