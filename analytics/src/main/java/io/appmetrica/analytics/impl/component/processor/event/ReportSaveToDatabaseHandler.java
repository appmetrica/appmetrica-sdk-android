package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportSaveToDatabaseHandler extends ReportComponentHandler {

    public ReportSaveToDatabaseHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CounterReport reportData) {
        getComponent().getEventSaver().identifyAndSaveReport(reportData);
        return false;
    }
}
