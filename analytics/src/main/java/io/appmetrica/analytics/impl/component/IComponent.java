package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;

public interface IComponent extends IBaseComponent {

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

    @NonNull
    CounterConfigurationReporterType getReporterType();
}
