package io.appmetrica.analytics.impl;

import android.os.Handler;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;

public interface IAppMetricaCore {

    void activate(@NonNull AppMetricaConfig config,
                  @NonNull IReporterFactoryProvider reporterFactoryProvider);

    @NonNull
    Handler getMetricaHandler();

    @NonNull
    ClientTimeTracker getClientTimeTracker();

    @NonNull
    ICommonExecutor getExecutor();

    @NonNull
    AppOpenWatcher getAppOpenWatcher();
}
