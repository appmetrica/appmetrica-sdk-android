package io.appmetrica.analytics.impl.component.processor;

import java.util.List;

public abstract class EventProcessingStrategy<BaseHandler> {
    public abstract List<? extends BaseHandler> getEventHandlers();
}
