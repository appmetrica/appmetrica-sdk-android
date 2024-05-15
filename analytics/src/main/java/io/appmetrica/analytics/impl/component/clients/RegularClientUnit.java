package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class RegularClientUnit extends AbstractClientUnit {

    private static final String TAG = "[RegularClientUnit]";

    public RegularClientUnit(@NonNull Context context,
                             @NonNull RegularDispatcherComponent componentUnit) {
        super(context, componentUnit);
    }

    @Override
    protected void handleReport(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
        DebugLogger.info(
            TAG,
            "handle report for client unit: %s; data: %s",
            sdkConfig.startupArguments,
            report
        );
        getComponentUnit().handleReport(report, sdkConfig);
    }
}
