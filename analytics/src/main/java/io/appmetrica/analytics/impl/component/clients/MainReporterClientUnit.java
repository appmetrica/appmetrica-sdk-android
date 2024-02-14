package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.internal.YLogger;

public class MainReporterClientUnit extends AbstractClientUnit {

    MainReporterClientUnit(@NonNull Context context,
                           @NonNull RegularDispatcherComponent componentUnit) {

        super(context, componentUnit);
    }

    @Override
    protected void handleReport(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
        updateLocationTracking(WrapUtils.getOrDefault(
                sdkConfig.componentArguments.locationTracking,
                DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED));
        getComponentUnit().handleReport(report, sdkConfig);
    }

    @VisibleForTesting
    void updateLocationTracking(boolean enabled) {
        YLogger.d("Update location status for %s: enabled = %b", getComponentUnit().toString(), enabled);
        GlobalServiceLocator.getInstance().getLocationClientApi().updateTrackingStatusFromClient(enabled);
    }
}
