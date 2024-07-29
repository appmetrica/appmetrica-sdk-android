package io.appmetrica.analytics.impl.stub;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.AppOpenWatcher;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.ClientTimeTracker;
import io.appmetrica.analytics.impl.IAppMetricaCore;
import io.appmetrica.analytics.impl.IReporterFactoryProvider;
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController;

public class AppMetricaCoreStub implements IAppMetricaCore {

    @NonNull
    private final Handler metricaHandler;
    @NonNull
    private final IHandlerExecutor executor;
    @NonNull
    private final ClientTimeTracker clientTimeTracker;

    public AppMetricaCoreStub() {
        this.executor = ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor();
        this.metricaHandler = this.executor.getHandler();
        this.clientTimeTracker = new ClientTimeTracker();
    }

    @Override
    public void activate(@Nullable AppMetricaConfig config,
                         @NonNull IReporterFactoryProvider reporterFactoryProvider) {
        //Do nothing
    }

    @NonNull
    @Override
    public ClientTimeTracker getClientTimeTracker() {
        return clientTimeTracker;
    }

    @NonNull
    @Override
    public ICommonExecutor getDefaultExecutor() {
        return executor;
    }

    @NonNull
    @Override
    public AppOpenWatcher getAppOpenWatcher() {
        return new AppOpenWatcher();
    }

    @NonNull
    @Override
    public Handler getDefaultHandler() {
        return metricaHandler;
    }

    @NonNull
    @Override
    public JvmCrashClientController getJvmCrashClientController() {
        return new JvmCrashClientController();
    }
}
