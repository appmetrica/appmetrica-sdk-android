package io.appmetrica.analytics.impl.component.processor.session;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;

public class ReportSessionStopDueCrashHandler extends ReportComponentHandler {

    public ReportSessionStopDueCrashHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CounterReport reportData) {
        getComponent().getEventTrigger().trigger();
        getComponent().getSessionManager().stopCurrentSessionDueToCrash(reportData);
        return true;
    }
}
