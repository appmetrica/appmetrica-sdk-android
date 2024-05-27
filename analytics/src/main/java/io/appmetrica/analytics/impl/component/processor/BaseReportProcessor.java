package io.appmetrica.analytics.impl.component.processor;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;

public class BaseReportProcessor<T, C extends IComponent> {

    private static final String TAG = "[BaseReportProcessor]";

    protected interface ProcessItem<T> {

        boolean process(T handler, CounterReport report);

    }

    private final ProcessingStrategyFactory<T> mProcessingStrategyFactory;
    private final C mComponent;

    protected BaseReportProcessor(final ProcessingStrategyFactory<T> processingStrategyFactory,
                                  final C component) {
        mProcessingStrategyFactory = processingStrategyFactory;
        mComponent = component;
    }

    protected boolean process(@NonNull CounterReport report,
                              @NonNull ProcessItem<T> processItem) {
        List<? extends  T> handlers = getStrategy(report).getEventHandlers();
        for (T handler : handlers) {
            DebugLogger.INSTANCE.info(
                TAG,
                "For component %s processing report (of type %s) %s with handler: %s",
                mComponent.getComponentId(),
                InternalEvents.valueOf(report.getType()).getInfo(),
                report,
                handler.getClass().getSimpleName()
            );
            if (processItem.process(handler, report)) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Stop processing report %s because %s returned true",
                    report,
                    handler.getClass().getSimpleName()
                );
                return true;
            }
        }
        return false;
    }

    EventProcessingStrategy<T> getStrategy(final CounterReport report) {
        return mProcessingStrategyFactory.getProcessingStrategy(report.getType());
    }

    protected C getComponent() {
        return mComponent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public ProcessingStrategyFactory<T> getProcessingStrategyFactory() {
        return mProcessingStrategyFactory;
    }
}
