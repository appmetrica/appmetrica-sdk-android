package io.appmetrica.analytics.impl.component.processor.session;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;

public class ReportSessionActivityStartHandler extends ReportComponentHandler {

    public ReportSessionActivityStartHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CoreServiceEvent serviceEvent) {
        ComponentUnit component = getComponent();
        component.getSessionManager().heartbeat(serviceEvent);
        return false;
    }
}
