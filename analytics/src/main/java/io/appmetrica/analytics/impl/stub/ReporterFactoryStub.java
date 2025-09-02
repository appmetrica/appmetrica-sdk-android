package io.appmetrica.analytics.impl.stub;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.AppMetricaConfigExtension;
import io.appmetrica.analytics.impl.IMainReporter;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.IReporterFactory;
import io.appmetrica.analytics.impl.IUnhandledSituationReporter;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class ReporterFactoryStub implements IReporterFactory {

    @NonNull
    @Override
    public IMainReporter buildOrUpdateAnonymousMainReporter(
        @NonNull AppMetricaConfig config,
        @NonNull PublicLogger logger,
        @NonNull AppMetricaConfigExtension configExtension
    ) {
        return new MainReporterStub();
    }

    @NonNull
    @Override
    public IMainReporter buildOrUpdateMainReporter(
        @NonNull AppMetricaConfig config,
        @NonNull PublicLogger logger,
        @NonNull AppMetricaConfigExtension configExtension
    ) {
        return new MainReporterStub();
    }

    @Override
    public void activateReporter(@NonNull ReporterConfig config) {
        //Do nothing
    }

    @NonNull
    @Override
    public IReporterExtended getOrCreateReporter(@NonNull ReporterConfig config) {
        return new ReporterExtendedStub();
    }

    @NonNull
    @Override
    public IUnhandledSituationReporter getUnhandhedSituationReporter(@NonNull AppMetricaConfig config) {
        return new ReporterExtendedStub();
    }

    @NonNull
    @Override
    public IReporterFactory getReporterFactory() {
        return this;
    }
}
