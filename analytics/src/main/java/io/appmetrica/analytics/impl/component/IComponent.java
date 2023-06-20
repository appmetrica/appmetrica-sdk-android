package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;

public interface IComponent extends IBaseComponent {

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

    @NonNull
    CounterConfigurationReporterType getReporterType();
}
