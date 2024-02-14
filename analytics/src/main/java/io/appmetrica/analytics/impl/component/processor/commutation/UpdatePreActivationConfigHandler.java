package io.appmetrica.analytics.impl.component.processor.commutation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.logger.internal.YLogger;

public class UpdatePreActivationConfigHandler extends CommutationHandler {

    private final DataSendingRestrictionControllerImpl mRestrictionController;

    public UpdatePreActivationConfigHandler(@NonNull CommutationDispatcherComponent component,
                                            @NonNull DataSendingRestrictionControllerImpl restrictionController) {
        super(component);
        mRestrictionController = restrictionController;
    }

    @Override
    public boolean process(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit) {
        CommonArguments.ReporterArguments counterConfiguration = clientUnit.getComponent().getConfiguration();
        mRestrictionController.setEnabledFromMainReporter(counterConfiguration.dataSendingEnabled);
        updateTrackingLocationStatus(
                counterConfiguration.locationTracking
        );
        return false;
    }

    private void updateTrackingLocationStatus(@Nullable Boolean trackingEnabled) {
        YLogger.d("Update location status for %s: enabled = %s",
                getComponent().getComponentId().toString(),
                String.valueOf(trackingEnabled)
        );

        if (BooleanUtils.isTrue(trackingEnabled)) {
            GlobalServiceLocator.getInstance().getLocationClientApi().updateTrackingStatusFromClient(true);
        } else if (BooleanUtils.isFalse(trackingEnabled)) {
            GlobalServiceLocator.getInstance().getLocationClientApi().updateTrackingStatusFromClient(false);
        }
    }
}
