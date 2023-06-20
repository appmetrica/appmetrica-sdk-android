package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.EventHandler;

public class ReportingReportProcessor<T extends EventHandler, C extends ComponentUnit>
        extends BaseReportProcessor<T, C> {

    public ReportingReportProcessor(final ProcessingStrategyFactory<T> processingStrategyFactory,
                                    final C component) {
        super(processingStrategyFactory, component);
    }

    public boolean process(final CounterReport report) {
        return process(report, new ProcessItem<T>() {
            @Override
            public boolean process(T handler, CounterReport report) {
                return handler.process(report);
            }
        });
    }

}
