package io.appmetrica.analytics.impl.component.processor;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;

public class BaseReportProcessor<T, C extends IComponent> {

    private static final String TAG = "[BaseReportProcessor]";

    protected interface ProcessItem<T> {

        boolean process(T handler, CoreServiceEvent serviceEvent);

    }

    private final ProcessingStrategyFactory<T> mProcessingStrategyFactory;
    private final C mComponent;

    protected BaseReportProcessor(final ProcessingStrategyFactory<T> processingStrategyFactory,
                                  final C component) {
        mProcessingStrategyFactory = processingStrategyFactory;
        mComponent = component;
    }

    protected boolean process(@NonNull CoreServiceEvent serviceEvent,
                              @NonNull ProcessItem<T> processItem) {
        List<? extends  T> handlers = getStrategy(serviceEvent).getEventHandlers();
        for (T handler : handlers) {
            DebugLogger.INSTANCE.info(
                TAG,
                "For component %s processing report (of type %s) %s with handler: %s",
                mComponent.getComponentId(),
                InternalEvents.valueOf(serviceEvent.getType()).getInfo(),
                serviceEvent,
                handler.getClass().getSimpleName()
            );
            if (processItem.process(handler, serviceEvent)) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Stop processing report %s because %s returned true",
                    serviceEvent,
                    handler.getClass().getSimpleName()
                );
                return true;
            }
        }
        return false;
    }

    EventProcessingStrategy<T> getStrategy(final CoreServiceEvent serviceEvent) {
        return mProcessingStrategyFactory.getProcessingStrategy(serviceEvent.getType());
    }

    protected C getComponent() {
        return mComponent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public ProcessingStrategyFactory<T> getProcessingStrategyFactory() {
        return mProcessingStrategyFactory;
    }
}
