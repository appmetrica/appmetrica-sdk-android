package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.startup.StartupListener;

public interface IReportableComponent extends StartupListener {

    void handleReport(@NonNull CoreServiceEvent serviceEvent);

    void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig);

}


