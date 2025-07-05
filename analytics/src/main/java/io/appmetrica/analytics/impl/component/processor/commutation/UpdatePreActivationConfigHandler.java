package io.appmetrica.analytics.impl.component.processor.commutation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.location.LocationClientApi;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UpdatePreActivationConfigHandler extends CommutationHandler {

    private static final String TAG = "[UpdatePreActivationConfigHandler]";

    private final DataSendingRestrictionControllerImpl mRestrictionController;

    public UpdatePreActivationConfigHandler(@NonNull CommutationDispatcherComponent component,
                                            @NonNull DataSendingRestrictionControllerImpl restrictionController) {
        super(component);
        mRestrictionController = restrictionController;
    }

    @Override
    public boolean process(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit) {
        DebugLogger.INSTANCE.info(TAG, "process: %s", reportData);
        CommonArguments.ReporterArguments counterConfiguration = clientUnit.getComponent().getConfiguration();
        mRestrictionController.setEnabledFromMainReporter(counterConfiguration.dataSendingEnabled);
        updateLocationStatus(counterConfiguration);
        updateTrackingAdvIdentifiersStatus(counterConfiguration.advIdentifiersTrackingEnabled);
        return false;
    }

    private void updateDataSendingStatus(@Nullable Boolean dataSendingEnabled) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Update data sending status for %s: enabled = %s",
            getComponent().getComponentId().toString(),
            dataSendingEnabled
        );
        if (dataSendingEnabled != null) {
            mRestrictionController.setEnabledFromMainReporter(dataSendingEnabled);
        }
    }

    private void updateTrackingAdvIdentifiersStatus(@Nullable Boolean trackingEnabled) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Update adv identifiers tracking status for %s: enabled = %s",
            getComponent().getComponentId().toString(),
            trackingEnabled
        );
        GlobalServiceLocator.getInstance().getAdvertisingIdGetter().updateStateFromClientConfig(
            trackingEnabled == null ? DefaultValues.DEFAULT_REPORT_ADV_IDENTIFIERS_ENABLED : trackingEnabled
        );
    }

    private void updateLocationStatus(@NonNull CommonArguments.ReporterArguments arguments) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Update location status for %s: enabled = %s; location = %s",
            getComponent().getComponentId().toString(),
            String.valueOf(arguments.locationTracking),
            String.valueOf(arguments.manualLocation)
        );

        LocationClientApi locationClientApi = GlobalServiceLocator.getInstance().getLocationClientApi();

        if (BooleanUtils.isTrue(arguments.locationTracking)) {
            locationClientApi.updateTrackingStatusFromClient(true);
        } else if (BooleanUtils.isFalse(arguments.locationTracking)) {
            locationClientApi.updateTrackingStatusFromClient(false);
        }
        locationClientApi.updateLocationFromClient(arguments.manualLocation);
    }
}
