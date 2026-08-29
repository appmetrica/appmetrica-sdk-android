package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.EventSessionProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;

public class ReportSessionHandler extends ReportComponentHandler {

    private final ReportingReportProcessor<ReportComponentHandler, ComponentUnit> mReportSessionProcessor;

    public ReportSessionHandler(ComponentUnit component) {
        super(component);
        mReportSessionProcessor =
                new ReportingReportProcessor<ReportComponentHandler, ComponentUnit>(
                        new EventSessionProcessingStrategyFactory(component),
                        component
                );
    }

    @Override
    public boolean process(@NonNull CoreServiceEvent serviceEvent) {
        return mReportSessionProcessor.process(serviceEvent);
    }

    @VisibleForTesting
    ReportSessionHandler(ComponentUnit componentUnit,
                         ReportingReportProcessor<ReportComponentHandler,
                                                  ComponentUnit> reportSessionProcessor) {
        super(componentUnit);
        mReportSessionProcessor = reportSessionProcessor;
    }
}
