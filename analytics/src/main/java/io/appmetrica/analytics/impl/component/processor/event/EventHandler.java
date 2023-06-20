package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;

public interface EventHandler {

    /**
     * Logic of report processing.
     * @param reportData report to handle.
     * @return true, if handler should break processing chain, false - otherwise.
     */
    boolean process(@NonNull CounterReport reportData);

}
