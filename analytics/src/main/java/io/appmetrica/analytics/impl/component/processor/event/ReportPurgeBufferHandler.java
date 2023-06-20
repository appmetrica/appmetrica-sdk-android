package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class ReportPurgeBufferHandler extends ReportComponentHandler {

    public ReportPurgeBufferHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        // Forcible push out from the events buffer
        getComponent().flushEvents();
        return false;
    }
}
