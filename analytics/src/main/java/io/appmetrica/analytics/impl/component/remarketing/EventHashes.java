package io.appmetrica.analytics.impl.component.remarketing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.HashSet;
import java.util.Set;

public class EventHashes {

    private boolean mTreatUnknownEventAsNew;
    @NonNull
    private Set<Integer> mEventNameHashes;

    private int mLastVersionCode;
    private int mHashesCountFromLastVersion;

    public EventHashes() {
        this(false, 0, 0, new HashSet<Integer>());
    }

    public EventHashes(final boolean treatUnknownEventAsNew,
                       final int lastVersionCode,
                       final int hashesCountFromLastVersion,
                       @NonNull final int[] eventNameHashes) {
        this(
                treatUnknownEventAsNew,
                lastVersionCode,
                hashesCountFromLastVersion,
                CollectionUtils.hashSetFromIntArray(eventNameHashes)
        );
    }

    public EventHashes(final boolean treatUnknownEventAsNew,
                       final int lastVersionCode,
                       final int hashesCountFromLastVersion,
                       @NonNull final Set<Integer> eventNameHashes) {
        mTreatUnknownEventAsNew = treatUnknownEventAsNew;
        mEventNameHashes = eventNameHashes;
        mLastVersionCode = lastVersionCode;
        mHashesCountFromLastVersion = hashesCountFromLastVersion;
    }

    public void clearEventHashes() {
        mEventNameHashes = new HashSet<Integer>();
        mHashesCountFromLastVersion = 0;
    }

    public boolean treatUnknownEventAsNew() {
        return mTreatUnknownEventAsNew;
    }

    public void setTreatUnknownEventAsNew(final boolean treatUnknownEventAsNew) {
        mTreatUnknownEventAsNew = treatUnknownEventAsNew;
    }

    @NonNull
    public Set<Integer> getEventNameHashes() {
        return mEventNameHashes;
    }

    public int getHashesCountFromLastVersion() {
        return mHashesCountFromLastVersion;
    }

    public int getLastVersionCode() {
        return mLastVersionCode;
    }

    public void setLastVersionCode(final int lastVersionCode) {
        mLastVersionCode = lastVersionCode;
        mHashesCountFromLastVersion = 0;
    }

    public void addEventNameHash(int hashCode) {
        mEventNameHashes.add(hashCode);
        mHashesCountFromLastVersion ++;
    }
}
