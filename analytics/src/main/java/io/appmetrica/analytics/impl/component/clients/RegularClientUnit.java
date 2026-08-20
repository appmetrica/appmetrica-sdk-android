package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class RegularClientUnit extends AbstractClientUnit {

    private static final String TAG = "[RegularClientUnit]";

    public RegularClientUnit(@NonNull Context context,
                             @NonNull RegularDispatcherComponent componentUnit) {
        super(context, componentUnit);
    }

    @Override
    protected void handleReport(@NonNull ServiceEvent serviceEvent, @NonNull CommonArguments sdkConfig) {
        DebugLogger.INSTANCE.info(
            TAG,
            "handle serviceEvent for client unit: %s; data: %s",
            sdkConfig.startupArguments,
            serviceEvent
        );
        getComponentUnit().handleReport(serviceEvent, sdkConfig);
    }
}
