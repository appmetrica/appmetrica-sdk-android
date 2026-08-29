package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;

public class ReportFirstOccurrenceStatusHandler extends ReportComponentHandler {

    @NonNull
    private final EventFirstOccurrenceService mEventFirstOccurrenceService;

    public ReportFirstOccurrenceStatusHandler(@NonNull final ComponentUnit component) {
        this(component, component.getEventFirstOccurrenceService());
    }

    @Override
    public boolean process(@NonNull final CoreServiceEvent serviceEvent) {
        String eventName = serviceEvent.getName();
        if (!StringUtils.isNullOrEmpty(eventName)) {
            serviceEvent.setFirstOccurrenceStatus(mEventFirstOccurrenceService
                    .checkFirstOccurrence(serviceEvent.getName()));
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
