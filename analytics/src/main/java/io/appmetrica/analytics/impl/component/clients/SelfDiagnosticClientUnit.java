package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SelfDiagnosticClientUnit implements ClientUnit {

    private static final String TAG = "[SelfDiagnosticClientUnit]";

    @Nullable
    private final RegularDispatcherComponent mComponentUnit;

    public SelfDiagnosticClientUnit(@Nullable RegularDispatcherComponent componentUnit) {
        mComponentUnit = componentUnit;
    }

    @Override
    public void handle(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
        if (mComponentUnit != null) {
            YLogger.debug(TAG, "handle report %s with sdkConfig %s", report, sdkConfig);
            mComponentUnit.handleReport(report, sdkConfig);
        } else {
            YLogger.debug(TAG, "ComponentUnit is null. Will not handle report %s with sdkConfig", report, sdkConfig);
        }
    }

    @Override
    public void onDisconnect() {
        // do nothing
    }
}
