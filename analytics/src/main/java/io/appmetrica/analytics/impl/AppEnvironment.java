package io.appmetrica.analytics.impl;

import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.logger.internal.DebugLogger;

/**
 * Accumulate event's environment changes on service side. After adding or removing value
 * {@link AppEnvironment#getLastRevision()} revision number will be incremented and new environment
 * will be saved by caller.
 */
public class AppEnvironment {

    public static final String DEFAULT_ENVIRONMENT_JSON_STRING = "{}";
    public static final long DEFAULT_ENVIRONMENT_REVISION = 0;

    public static final String TAG = "[App Environment]";

    public static final class EnvironmentRevision {
        public final String value;
        public final long revisionNumber;

        public EnvironmentRevision(String value, long revisionNumber) {
            this.value = value;
            this.revisionNumber = revisionNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EnvironmentRevision revision = (EnvironmentRevision) o;

            if (revisionNumber != revision.revisionNumber) return false;
            return !(value != null ? !value.equals(revision.value) : revision.value != null);

        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + (int) (revisionNumber ^ (revisionNumber >>> 32));
            return result;
        }
    }

    private MeasuredJsonMap mValues;
    private long mRevisionNumber;
    private boolean mChanged;

    @NonNull
    private final SimpleMapLimitation mSimpleMapLimitation;

    public AppEnvironment(String value, long latestRevision, @NonNull PublicLogger logger) {
        this(
                value,
                latestRevision,
                new SimpleMapLimitation(logger, TAG)
        );
    }

    @VisibleForTesting
    AppEnvironment(String value,
                   long latestRevision,
                   @NonNull SimpleMapLimitation mapLimitation) {
        mRevisionNumber = latestRevision;
        try {
            mValues = new MeasuredJsonMap(value);
        } catch (Throwable e) {
            DebugLogger.error(TAG, e, "Some problems during parse %s", value);
            mValues = new MeasuredJsonMap();
        }
        mSimpleMapLimitation = mapLimitation;
    }

    public synchronized void reset() {
        mValues = new MeasuredJsonMap();
    }

    public synchronized void add(@NonNull Pair<String, String> pair) {
        if (mSimpleMapLimitation.tryToAddValue(mValues, pair.first, pair.second)) {
            mChanged = true;
        }
    }

    @VisibleForTesting
    synchronized void add(@NonNull String key, String value) {
        if (mSimpleMapLimitation.tryToAddValue(mValues, key, value)) {
            mChanged = true;
        }
    }

    public synchronized EnvironmentRevision getLastRevision() {
        if (mChanged) {
            mRevisionNumber++;
            mChanged = false;
        }
        return new EnvironmentRevision(JsonHelper.mapToJsonString(mValues), mRevisionNumber);
    }

    @VisibleForTesting
    MeasuredJsonMap getValues() {
        return mValues;
    }

    @Override
    public synchronized String toString() {
        StringBuilder builder = new StringBuilder("Map size ");
        builder.append(mValues.size());
        builder.append(". Is changed ");
        builder.append(mChanged);
        builder.append(". Current revision ");
        builder.append(mRevisionNumber);
        return builder.toString();
    }
}
