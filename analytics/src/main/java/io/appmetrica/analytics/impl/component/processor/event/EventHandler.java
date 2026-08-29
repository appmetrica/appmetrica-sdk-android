package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoreServiceEvent;

public interface EventHandler {

    /**
     * Logic of report processing.
     * @param serviceEvent report to handle.
     * @return true, if handler should break processing chain, false - otherwise.
     */
    boolean process(@NonNull CoreServiceEvent serviceEvent);

}
