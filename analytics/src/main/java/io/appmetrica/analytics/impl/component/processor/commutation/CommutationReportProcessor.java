package io.appmetrica.analytics.impl.component.processor.commutation;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.component.processor.BaseReportProcessor;
import io.appmetrica.analytics.impl.component.processor.ProcessingStrategyFactory;

public class CommutationReportProcessor<T extends CommutationHandler, C extends CommutationDispatcherComponent>
        extends BaseReportProcessor<T, C> {

    public CommutationReportProcessor(@NonNull ProcessingStrategyFactory<T> processingStrategyFactory,
                               @NonNull C component) {
        super(processingStrategyFactory, component);
    }

    public boolean process(@NonNull CounterReport report, @NonNull final CommutationClientUnit clientUnit) {
        return process(report, new ProcessItem<T>() {
            @Override
            public boolean process(T handler, CounterReport report) {
                return handler.process(report, clientUnit);
            }
        });
    }
}
