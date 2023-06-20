package io.appmetrica.analytics.impl.component.processor.commutation;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;

public abstract class CommutationHandler {

    @NonNull
    private final CommutationDispatcherComponent mComponent;

    // Constructs a new report mediator for component
    // NOTE: Component is mutable during processing

    CommutationHandler(@NonNull CommutationDispatcherComponent component) {
        mComponent = component;
    }

    @NonNull
    protected CommutationDispatcherComponent getComponent() {
        return mComponent;
    }

    /**
     * Logic of report processing.
     * @param reportData report to handle.
     * @return true, if handler should break processing chain, false - otherwise.
     */
    public abstract boolean process(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit);
}
