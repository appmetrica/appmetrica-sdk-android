package io.appmetrica.analytics.impl.events;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventTrigger {

    @NonNull
    private final List<EventCondition> mConditions;
    @NonNull
    private final EventsFlusher mEventsFlusher;

    private final AtomicBoolean mTriggerEnabled;

    public EventTrigger(@NonNull List<EventCondition> conditions, @NonNull EventsFlusher eventsFlusher) {
        mConditions = conditions;
        mEventsFlusher = eventsFlusher;
        mTriggerEnabled = new AtomicBoolean(true);
    }

    public void trigger() {
        if (mTriggerEnabled.get()) {
            sendEventsIfNeeded();
        }
    }

    public void enableTrigger() {
        mTriggerEnabled.set(true);
    }

    public void disableTrigger() {
        mTriggerEnabled.set(false);
    }

    private void sendEventsIfNeeded() {
        if (mConditions.isEmpty()) {
            sendEvents();
        } else {
            boolean areConditionsMet = false;
            for (EventCondition condition : mConditions) {
                areConditionsMet |= condition.isConditionMet();
            }
            if (areConditionsMet) {
                sendEvents();
            }
        }
    }

    private void sendEvents() {
        mEventsFlusher.flushEvents();
    }
}
