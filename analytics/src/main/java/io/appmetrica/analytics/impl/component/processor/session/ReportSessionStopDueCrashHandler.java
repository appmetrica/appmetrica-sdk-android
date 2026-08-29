package io.appmetrica.analytics.impl.component.processor.session;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;

public class ReportSessionStopDueCrashHandler extends ReportComponentHandler {

    public ReportSessionStopDueCrashHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CoreServiceEvent serviceEvent) {
        getComponent().getEventTrigger().trigger();
        getComponent().getSessionManager().stopCurrentSessionDueToCrash(serviceEvent);
        return true;
    }
}
