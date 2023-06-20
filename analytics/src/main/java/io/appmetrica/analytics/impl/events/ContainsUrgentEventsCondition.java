package io.appmetrica.analytics.impl.events;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class ContainsUrgentEventsCondition implements EventCondition, EventListener {

    private final Set<Integer> mUrgentEvents;

    private AtomicLong mUrgentEventsCount;

    public ContainsUrgentEventsCondition(@NonNull DatabaseHelper databaseHelper) {
        mUrgentEvents = new HashSet<Integer>();
        mUrgentEvents.add(InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId());
        mUrgentEvents.add(InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId());
        mUrgentEvents.add(InternalEvents.EVENT_TYPE_INIT.getTypeId());
        mUrgentEvents.add(InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId());
        databaseHelper.addEventListener(this);
        mUrgentEventsCount = new AtomicLong(databaseHelper.getEventsOfFollowingTypesCount(mUrgentEvents));
    }

    @Override
    public boolean isConditionMet() {
        return mUrgentEventsCount.get() > 0;
    }

    @Override
    public void onEventsAdded(@NonNull List<Integer> reportTypes) {
        int newUrgentEventsNumber = 0;
        for (int reportType : reportTypes) {
            if (mUrgentEvents.contains(reportType)) {
                newUrgentEventsNumber++;
            }
        }
        mUrgentEventsCount.addAndGet(newUrgentEventsNumber);
    }

    @Override
    public void onEventsRemoved(@NonNull List<Integer> reportTypes) {
        int deletedUrgentEventsNumber = 0;
        for (int reportType : reportTypes) {
            if (mUrgentEvents.contains(reportType)) {
                deletedUrgentEventsNumber++;
            }
        }
        mUrgentEventsCount.addAndGet(-deletedUrgentEventsNumber);
    }
}
