package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportPauseForegroundSessionHandler extends ReportComponentHandler {

    public ReportPauseForegroundSessionHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CoreServiceEvent serviceEvent) {
        getComponent().getSessionManager().heartbeat(serviceEvent);
        return false;
    }
}
