package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;

public interface IComponent extends IBaseComponent {

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

    @NonNull
    CounterConfigurationReporterType getReporterType();

    /**
     * Called when the component becomes inactive (e.g., all clients disconnected).
     * This is a hint that the component may be stopped soon and should flush any pending data.
     */
    void onBecomeInactive();
}
