package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.List;

public class EventProcessingDefaultStrategy<BaseHandler> extends EventProcessingStrategy<BaseHandler> {

    private final List<BaseHandler> mEventHandlers;

    public EventProcessingDefaultStrategy(List<BaseHandler> eventHandlers) {
        mEventHandlers = CollectionUtils.unmodifiableListCopy(eventHandlers);
    }

    @Override
    public List<? extends BaseHandler> getEventHandlers() {
        return mEventHandlers;
    }
}
