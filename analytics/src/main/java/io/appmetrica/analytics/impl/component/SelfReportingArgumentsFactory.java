package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class SelfReportingArgumentsFactory extends ReportRequestConfig.BaseDataSendingStrategy {

    public SelfReportingArgumentsFactory(@NonNull DataSendingRestrictionControllerImpl controller) {
        super(controller);
    }

    @Override
    public boolean shouldSend(@Nullable Boolean fromArguments) {
        return !controller.isRestrictedForSdk() && super.shouldSend(fromArguments);
    }

}
