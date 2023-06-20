package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;

public interface ClientUnit {

    void handle(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig);

    void onDisconnect();

}
