package io.appmetrica.analytics.impl.component.remarketing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.FirstOccurrenceStatus;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.logger.internal.YLogger;

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

        YLogger.d(
                "%sReturn status: %s for eventName: %s. Current service status: hashesCount = %d for version: " +
                        "%d with treatUnknownEventAsNew: %b",
                TAG,
                status.name(),
                eventName,
                mEventHashes.getHashesCountFromLastVersion(),
                mEventHashes.getLastVersionCode(),
                mEventHashes.treatUnknownEventAsNew()
        );

        return status;
    }

    public void reset() {
        YLogger.d("%sReset", TAG);
        if (mEventHashes == null) {
            readEventHashes();
        }
        mEventHashes.clearEventHashes();
        mEventHashes.setTreatUnknownEventAsNew(true);
        save();
    }

    private void readEventHashes() {
        YLogger.d("%sRead data from storage", TAG);
        mEventHashes = mEventHashesStorage.read();
        if (mEventHashes.getLastVersionCode() != mCurrentVersionCode) {
            YLogger.d("%sUpdate version", TAG);
            mEventHashes.setLastVersionCode(mCurrentVersionCode);
            save();
        }
    }

    private void save() {
        YLogger.d("%sSave data to storage", TAG);
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
