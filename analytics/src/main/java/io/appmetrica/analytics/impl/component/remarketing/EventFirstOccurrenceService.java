package io.appmetrica.analytics.impl.component.remarketing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.FirstOccurrenceStatus;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class EventFirstOccurrenceService {
    private static final String TAG = "[EventFirstOccurrenceService]";

    private static final int MAX_HASHES_COUNT_PER_VERSION = 1000;

    private final int mCurrentVersionCode;
    @NonNull
    private final EventHashesStorage mEventHashesStorage;
    @Nullable
    private EventHashes mEventHashes;

    public EventFirstOccurrenceService(@NonNull final Context context,
                                       @NonNull final ComponentId componentId,
                                       final int currentVersionCode) {
        this(new EventHashesStorage(context, componentId), currentVersionCode);
    }

    @NonNull
    public FirstOccurrenceStatus checkFirstOccurrence(@NonNull final String eventName) {
        if (mEventHashes == null) {
            readEventHashes();
        }

        FirstOccurrenceStatus status;
        int eventHash = getHash(eventName);
        if (mEventHashes.getEventNameHashes().contains(eventHash)) {
            status = FirstOccurrenceStatus.NON_FIRST_OCCURENCE;
        } else {
            status = mEventHashes.treatUnknownEventAsNew() ? FirstOccurrenceStatus.FIRST_OCCURRENCE :
                    FirstOccurrenceStatus.UNKNOWN;

            if (mEventHashes.getHashesCountFromLastVersion() < MAX_HASHES_COUNT_PER_VERSION) {
                mEventHashes.addEventNameHash(eventHash);
            } else {
                mEventHashes.setTreatUnknownEventAsNew(false);
            }
            save();
        }

        DebugLogger.INSTANCE.info(
            TAG,
            "Return status: %s for eventName: %s. Current service status: hashesCount = %d for version: " +
                "%d with treatUnknownEventAsNew: %b",
            status.name(),
            eventName,
            mEventHashes.getHashesCountFromLastVersion(),
            mEventHashes.getLastVersionCode(),
            mEventHashes.treatUnknownEventAsNew()
        );

        return status;
    }

    public void reset() {
        DebugLogger.INSTANCE.info(TAG, "Reset");
        if (mEventHashes == null) {
            readEventHashes();
        }
        mEventHashes.clearEventHashes();
        mEventHashes.setTreatUnknownEventAsNew(true);
        save();
    }

    private void readEventHashes() {
        DebugLogger.INSTANCE.info(TAG, "Read data from storage");
        mEventHashes = mEventHashesStorage.read();
        if (mEventHashes.getLastVersionCode() != mCurrentVersionCode) {
            DebugLogger.INSTANCE.info(TAG, "Update version");
            mEventHashes.setLastVersionCode(mCurrentVersionCode);
            save();
        }
    }

    private void save() {
        DebugLogger.INSTANCE.info(TAG, "Save data to storage");
        mEventHashesStorage.write(mEventHashes);
    }

    private int getHash(@NonNull String eventName) {
        return eventName.hashCode();
    }

    @VisibleForTesting
    EventFirstOccurrenceService(@NonNull final EventHashesStorage eventHashesStorage, final int currentVersionCode) {
        mCurrentVersionCode = currentVersionCode;
        mEventHashesStorage = eventHashesStorage;
    }

    @VisibleForTesting
    public int getCurrentVersionCode() {
        return mCurrentVersionCode;
    }

    @NonNull
    @VisibleForTesting
    EventHashesStorage getEventHashesStorage() {
        return mEventHashesStorage;
    }
}
