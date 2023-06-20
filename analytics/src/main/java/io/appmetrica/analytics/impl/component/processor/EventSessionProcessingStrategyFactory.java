package io.appmetrica.analytics.impl.component.processor;

import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPauseForegroundSessionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveInitHandler;
import io.appmetrica.analytics.impl.component.processor.session.ReportSessionActivityStartHandler;
import java.util.LinkedList;

public class EventSessionProcessingStrategyFactory extends
        ProcessingStrategyFactory<ReportComponentHandler> {

    private final ReportSaveInitHandler mReportSaveInitHandler;
    private final ReportSessionActivityStartHandler mReportSessionActivityStartHandler;
    private final ReportPauseForegroundSessionHandler mReportPauseForegroundSessionHandler;

    public EventSessionProcessingStrategyFactory(ComponentUnit component) {
        mReportSaveInitHandler = new ReportSaveInitHandler(component);
        mReportSessionActivityStartHandler = new ReportSessionActivityStartHandler(component);
        mReportPauseForegroundSessionHandler = new ReportPauseForegroundSessionHandler(component);
    }

    @Override
    public EventProcessingStrategy<ReportComponentHandler> getProcessingStrategy(int eventTypeId) {
        final LinkedList<ReportComponentHandler> reportHandlers = new LinkedList<ReportComponentHandler>();
        InternalEvents eventType = InternalEvents.valueOf(eventTypeId);
        switch (eventType) {
            case EVENT_TYPE_START:
                reportHandlers.add(mReportSessionActivityStartHandler);
                reportHandlers.add(mReportSaveInitHandler);
                break;
            case EVENT_TYPE_INIT:
                reportHandlers.add(mReportSaveInitHandler);
                break;
            case EVENT_TYPE_UPDATE_FOREGROUND_TIME:
                reportHandlers.add(mReportPauseForegroundSessionHandler);
                break;
            default:
                //do nothing
                break;
        }
        return new EventProcessingDefaultStrategy<ReportComponentHandler>(reportHandlers);
    }

    @VisibleForTesting
    ReportSaveInitHandler getReportSaveInitHandler() {
        return mReportSaveInitHandler;
    }

    @VisibleForTesting
    ReportSessionActivityStartHandler getReportSessionActivityStartHandler() {
        return mReportSessionActivityStartHandler;
    }

    @VisibleForTesting
    ReportPauseForegroundSessionHandler getReportPauseForegroundSessionHandler() {
        return mReportPauseForegroundSessionHandler;
    }
}
