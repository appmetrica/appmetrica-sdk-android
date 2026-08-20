package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportAppEnvironmentClearedHandler extends ReportComponentHandler {

    public ReportAppEnvironmentClearedHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull ServiceEvent serviceEvent) {
        getComponent().clearAppEnvironment();
        return false;
    }
}
