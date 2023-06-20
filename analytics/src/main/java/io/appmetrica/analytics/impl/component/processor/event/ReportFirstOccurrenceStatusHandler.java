package io.appmetrica.analytics.impl.component.processor.event;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;

public class ReportFirstOccurrenceStatusHandler extends ReportComponentHandler {

    @NonNull
    private final EventFirstOccurrenceService mEventFirstOccurrenceService;

    public ReportFirstOccurrenceStatusHandler(@NonNull final ComponentUnit component) {
        this(component, component.getEventFirstOccurrenceService());
    }

    @Override
    public boolean process(@NonNull final CounterReport reportData) {
        String eventName = reportData.getName();
        if (TextUtils.isEmpty(eventName) == false) {
            reportData.setFirstOccurrenceStatus(mEventFirstOccurrenceService
                    .checkFirstOccurrence(reportData.getName()));
        }
        return false;
    }

    @VisibleForTesting
    ReportFirstOccurrenceStatusHandler(@NonNull final ComponentUnit componentUnit,
                                       @NonNull final EventFirstOccurrenceService eventFirstOccurrenceService) {
        super(componentUnit);
        mEventFirstOccurrenceService = eventFirstOccurrenceService;
    }

    @VisibleForTesting
    @NonNull
    EventFirstOccurrenceService getEventFirstOccurrenceService() {
        return mEventFirstOccurrenceService;
    }
}
