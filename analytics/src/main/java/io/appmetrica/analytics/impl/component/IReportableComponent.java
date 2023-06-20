package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.startup.StartupListener;

public interface IReportableComponent extends StartupListener {

    void handleReport(@NonNull CounterReport counterReport);

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

}


