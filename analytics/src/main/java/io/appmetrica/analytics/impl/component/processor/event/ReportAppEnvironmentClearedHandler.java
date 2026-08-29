package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportAppEnvironmentClearedHandler extends ReportComponentHandler {

    public ReportAppEnvironmentClearedHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CoreServiceEvent serviceEvent) {
        getComponent().clearAppEnvironment();
        return false;
    }
}
