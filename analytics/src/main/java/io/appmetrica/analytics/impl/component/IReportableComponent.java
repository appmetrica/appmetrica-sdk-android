package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.startup.StartupListener;

public interface IReportableComponent extends StartupListener {

    void handleReport(@NonNull ServiceEvent serviceEvent);

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

}


