package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class MainReporterArgumentsFactory extends ReportRequestConfig.BaseDataSendingStrategy {

    MainReporterArgumentsFactory(@NonNull DataSendingRestrictionControllerImpl controller) {
        super(controller);
    }
}
