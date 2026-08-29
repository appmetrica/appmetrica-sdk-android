package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SelfDiagnosticClientUnit implements ClientUnit {

    private static final String TAG = "[SelfDiagnosticClientUnit]";

    @Nullable
    private final RegularDispatcherComponent mComponentUnit;

    public SelfDiagnosticClientUnit(@Nullable RegularDispatcherComponent componentUnit) {
        mComponentUnit = componentUnit;
    }

    @Override
    public void handle(@NonNull CoreServiceEvent serviceEvent, @NonNull CommonArguments sdkConfig) {
        if (mComponentUnit != null) {
            DebugLogger.INSTANCE.info(TAG, "handle serviceEvent %s with sdkConfig %s", serviceEvent, sdkConfig);
            mComponentUnit.handleReport(serviceEvent, sdkConfig);
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "ComponentUnit is null. Will not handle serviceEvent %s with sdkConfig",
                serviceEvent,
                sdkConfig
            );
        }
    }

    @Override
    public void onDisconnect() {
        // do nothing
    }
}
