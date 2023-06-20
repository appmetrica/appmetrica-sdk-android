package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public abstract class ReportComponentHandler implements EventHandler {

    private final ComponentUnit mComponent;

    // Constructs a new report mediator for component
    // NOTE: Component is mutable during processing

    protected ReportComponentHandler(@NonNull final ComponentUnit component) {
        mComponent = component;
    }

    @NonNull
    protected ComponentUnit getComponent() {
        return mComponent;
    }
}
