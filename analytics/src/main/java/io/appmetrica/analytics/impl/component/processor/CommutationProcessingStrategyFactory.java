package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.factory.CommutationHandlerProvider;
import java.util.ArrayList;

public class CommutationProcessingStrategyFactory extends ProcessingStrategyFactory<CommutationHandler> {

    private final CommutationHandlerProvider mProvider;

    public CommutationProcessingStrategyFactory(CommutationDispatcherComponent component) {
        mProvider = new CommutationHandlerProvider(component);
    }

    @Override
    public EventProcessingStrategy<CommutationHandler> getProcessingStrategy(int eventTypeId) {
        ArrayList<CommutationHandler> list = new ArrayList<CommutationHandler>();
        InternalEvents eventType = InternalEvents.valueOf(eventTypeId);
        switch (eventType) {
            case EVENT_TYPE_STARTUP:
                list.add(mProvider.getForceStartupHandler());
                break;
            case EVENT_TYPE_REQUEST_REFERRER:
                list.add(mProvider.getRequestReferrerHandler());
                break;
            case EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG:
                list.add(mProvider.getUpdatePreActivationConfig());
                break;
        }

        return new EventProcessingDefaultStrategy<CommutationHandler>(list);
    }
}
