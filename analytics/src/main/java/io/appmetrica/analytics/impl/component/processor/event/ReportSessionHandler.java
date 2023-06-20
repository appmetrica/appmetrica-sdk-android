package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
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
    public boolean process(@NonNull CounterReport reportData) {
        return mReportSessionProcessor.process(reportData);
    }

    @VisibleForTesting
    ReportSessionHandler(ComponentUnit componentUnit,
                         ReportingReportProcessor<ReportComponentHandler,
                                                  ComponentUnit> reportSessionProcessor) {
        super(componentUnit);
        mReportSessionProcessor = reportSessionProcessor;
    }
}
