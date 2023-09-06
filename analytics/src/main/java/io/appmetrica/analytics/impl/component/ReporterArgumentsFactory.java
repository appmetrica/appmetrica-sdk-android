package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class ReporterArgumentsFactory extends ReportRequestConfig.BaseDataSendingStrategy {

    ReporterArgumentsFactory(@NonNull DataSendingRestrictionControllerImpl controller) {
        super(controller);
    }

    @Override
    public boolean shouldSend(@Nullable Boolean fromArguments) {
        return !controller.isRestrictedForReporter()
                && super.shouldSend(fromArguments);
    }

}
