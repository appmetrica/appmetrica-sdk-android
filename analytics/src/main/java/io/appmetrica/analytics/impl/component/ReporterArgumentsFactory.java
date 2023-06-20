package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class ReporterArgumentsFactory extends ReportRequestConfig.BaseStatisticsSendingStrategy {

    ReporterArgumentsFactory(@NonNull StatisticsRestrictionControllerImpl controller) {
        super(controller);
    }

    @Override
    public boolean shouldSend(@Nullable Boolean fromArguments) {
        return !controller.isRestrictedForReporter()
                && super.shouldSend(fromArguments);
    }

}
