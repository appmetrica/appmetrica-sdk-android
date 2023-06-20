package io.appmetrica.analytics.impl.events;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MaxReportsCountReachedCondition implements EventCondition, EventListener {

    @NonNull
    private final ReportComponentConfigurationHolder mConfigHolder;

    private AtomicLong reportsCount;

    public MaxReportsCountReachedCondition(@NonNull DatabaseHelper databaseHelper,
                                           @NonNull ReportComponentConfigurationHolder configHolder) {
        mConfigHolder = configHolder;
        reportsCount = new AtomicLong(databaseHelper.getEventsCount());
        databaseHelper.addEventListener(this);
    }

    @Override
    public boolean isConditionMet() {
        return reportsCount.get() >= mConfigHolder.get().getMaxReportsCount();
    }

    @Override
    public void onEventsAdded(@NonNull List<Integer> reportTypes) {
        reportsCount.addAndGet(reportTypes.size());
    }

    @Override
    public void onEventsRemoved(@NonNull List<Integer> reportTypes) {
        reportsCount.addAndGet(-reportTypes.size());
    }
}
