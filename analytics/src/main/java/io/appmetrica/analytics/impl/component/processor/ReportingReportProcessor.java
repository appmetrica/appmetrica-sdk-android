package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.EventHandler;

public class ReportingReportProcessor<T extends EventHandler, C extends ComponentUnit>
        extends BaseReportProcessor<T, C> {

    public ReportingReportProcessor(final ProcessingStrategyFactory<T> processingStrategyFactory,
                                    final C component) {
        super(processingStrategyFactory, component);
    }

    public boolean process(final CoreServiceEvent serviceEvent) {
        return process(serviceEvent, new ProcessItem<T>() {
            @Override
            public boolean process(T handler, CoreServiceEvent serviceEvent) {
                return handler.process(serviceEvent);
            }
        });
    }

}
