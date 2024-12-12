package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.location.LocationClientApi;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class MainReporterClientUnit extends AbstractClientUnit {

    private static final String TAG = "[MainReporterClientUnit]";

    MainReporterClientUnit(@NonNull Context context,
                           @NonNull RegularDispatcherComponent componentUnit) {

        super(context, componentUnit);
    }

    @Override
    protected void handleReport(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
        updateLocationTracking(sdkConfig);
        Boolean advIdentifiersTracking = sdkConfig.componentArguments.advIdentifiersTrackingEnabled;
        updateAdvIdentifiersTracking(advIdentifiersTracking);
        getComponentUnit().handleReport(report, sdkConfig);
    }

    private void updateLocationTracking(CommonArguments sdkConfig) {
        boolean enabled = WrapUtils.getOrDefault(
            sdkConfig.componentArguments.locationTracking,
            DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED
        );
        DebugLogger.INSTANCE.info(
            TAG,
            "Update location status for %s: enabled = %b",
            getComponentUnit().toString(),
            enabled
        );
        LocationClientApi locationClientApi = GlobalServiceLocator.getInstance().getLocationClientApi();
        locationClientApi.updateTrackingStatusFromClient(enabled);
        Location location = sdkConfig.componentArguments.manualLocation;
        locationClientApi.updateLocationFromClient(location);
    }

    @VisibleForTesting
    void updateAdvIdentifiersTracking(@Nullable Boolean enabled) {
        if (enabled != null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Update advIdentifiersTracking for %s to enabled %s",
                getComponentUnit().toString(),
                enabled
            );
            GlobalServiceLocator.getInstance().getAdvertisingIdGetter().updateStateFromClientConfig(enabled);
        }
    }
}
