package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportPrevSessionCrashpadCrashHandler extends ReportComponentHandler {

    public ReportPrevSessionCrashpadCrashHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        getComponent().getEventSaver().saveReportFromPrevSession(reportData);
        return true;
    }
}
