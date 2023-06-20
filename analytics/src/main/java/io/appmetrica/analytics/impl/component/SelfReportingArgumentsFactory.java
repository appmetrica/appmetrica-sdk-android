package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class SelfReportingArgumentsFactory extends ReportRequestConfig.BaseStatisticsSendingStrategy {

    public SelfReportingArgumentsFactory(@NonNull StatisticsRestrictionControllerImpl controller) {
        super(controller);
    }

    @Override
    public boolean shouldSend(@Nullable Boolean fromArguments) {
        return !controller.isRestrictedForSdk() && super.shouldSend(fromArguments);
    }

}
