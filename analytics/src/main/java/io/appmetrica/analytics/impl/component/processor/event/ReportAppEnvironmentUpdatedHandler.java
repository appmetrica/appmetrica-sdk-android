package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportAppEnvironmentUpdatedHandler extends ReportComponentHandler {

    public ReportAppEnvironmentUpdatedHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        getComponent().addAppEnvironmentValue(reportData);
        return false;
    }
}
