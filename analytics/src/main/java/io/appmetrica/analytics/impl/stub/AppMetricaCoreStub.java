package io.appmetrica.analytics.impl.stub;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.AppOpenWatcher;
import io.appmetrica.analytics.impl.ClientTimeTracker;
import io.appmetrica.analytics.impl.IAppMetricaCore;
import io.appmetrica.analytics.impl.IReporterFactoryProvider;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

public class AppMetricaCoreStub implements IAppMetricaCore {

    @NonNull
    private final Handler metricaHandler;
    @NonNull
    private final ICommonExecutor executor;
    @NonNull
    private final ICommonExecutor apiProxyExecutor;
    @NonNull
    private final ClientTimeTracker clientTimeTracker;

    public AppMetricaCoreStub(@NonNull ClientExecutorProvider clientExecutorProvider) {
        this(
                clientExecutorProvider.getDefaultExecutor(),
                clientExecutorProvider.getDefaultExecutor().getHandler(),
                clientExecutorProvider.getApiProxyExecutor(),
                new ClientTimeTracker()
        );
    }

    @VisibleForTesting
    public AppMetricaCoreStub(@NonNull ICommonExecutor executor,
                                 @NonNull Handler metricaHandler,
                                 @NonNull ICommonExecutor apiProxyExecutor,
                                 @NonNull ClientTimeTracker clientTimeTracker) {
        this.executor = executor;
        this.metricaHandler = metricaHandler;
        this.apiProxyExecutor = apiProxyExecutor;
        this.clientTimeTracker = clientTimeTracker;
    }

    @Override
    public void activate(@NonNull AppMetricaConfig config,
                         @NonNull IReporterFactoryProvider reporterFactoryProvider) {
        //Do nothing
    }

    @NonNull
    @Override
    public Handler getMetricaHandler() {
        return metricaHandler;
    }

    @NonNull
    @Override
    public ClientTimeTracker getClientTimeTracker() {
        return clientTimeTracker;
    }

    @NonNull
    @Override
    public ICommonExecutor getExecutor() {
        return executor;
    }

    @NonNull
    @Override
    public AppOpenWatcher getAppOpenWatcher() {
        return new AppOpenWatcher(apiProxyExecutor);
    }
}
