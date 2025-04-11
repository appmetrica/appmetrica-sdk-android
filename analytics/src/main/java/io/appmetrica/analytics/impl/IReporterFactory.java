package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public interface IReporterFactory extends IReporterFactoryProvider {

    @WorkerThread
    @NonNull
    IMainReporter buildOrUpdateAnonymousMainReporter(
        @NonNull AppMetricaConfig config,
        @NonNull PublicLogger logger,
        boolean needToClearEnvironment
    );

    @WorkerThread
    @NonNull
    IMainReporter buildOrUpdateMainReporter(
        @NonNull AppMetricaConfig config,
        @NonNull PublicLogger logger,
        boolean needToClearEnvironment
    );

    void activateReporter(@NonNull ReporterConfig config);

    @NonNull
    IReporterExtended getOrCreateReporter(@NonNull ReporterConfig config);

    @NonNull
    IUnhandledSituationReporter getUnhandhedSituationReporter(@NonNull AppMetricaConfig config);
}
