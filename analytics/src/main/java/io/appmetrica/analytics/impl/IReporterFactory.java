package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;

public interface IReporterFactory extends IReporterFactoryProvider {

    @WorkerThread
    @NonNull
    IMainReporter buildMainReporter(@NonNull AppMetricaConfig config,
                                   final boolean needToClearEnvironment);

    void activateReporter(@NonNull ReporterConfig config);

    @NonNull
    IReporterExtended getOrCreateReporter(@NonNull ReporterConfig config);

    @NonNull
    IUnhandledSituationReporter getMainOrCrashReporter(@NonNull AppMetricaConfig config);
}
