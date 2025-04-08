package io.appmetrica.analytics.impl;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import java.util.List;
import java.util.Map;

public interface IAppMetricaImpl
        extends MetricaConfigurator, DataResultReceiver.Receiver, IReporterFactoryProvider {

    @WorkerThread
    void activate(@NonNull final AppMetricaConfig config);

    @WorkerThread
    void activateAnonymously(@NonNull AppMetricaLibraryAdapterConfig libraryAdapterConfig);

    @AnyThread
    @Nullable
    MainReporterApiConsumerProvider getMainReporterApiConsumerProvider();

    @WorkerThread
    void requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener listener);

    @WorkerThread
    void requestDeferredDeeplink(DeferredDeeplinkListener listener);

    @WorkerThread
    void activateReporter(@NonNull ReporterConfig config);

    @WorkerThread
    @NonNull
    IReporterExtended getReporter(@NonNull ReporterConfig config);

    @AnyThread
    @Nullable
    String getDeviceId();

    @AnyThread
    @NonNull
    AdvIdentifiersResult getCachedAdvIdentifiers();

    @AnyThread
    @Nullable
    Map<String, String> getClids();

    @AnyThread
    @NonNull
    FeaturesResult getFeatures();

    @WorkerThread
    void requestStartupParams(
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    );
}
