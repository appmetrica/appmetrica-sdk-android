package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class MainReporterArgumentsFactory extends ReportRequestConfig.BaseStatisticsSendingStrategy {

    MainReporterArgumentsFactory(@NonNull StatisticsRestrictionControllerImpl controller) {
        super(controller);
    }
}
