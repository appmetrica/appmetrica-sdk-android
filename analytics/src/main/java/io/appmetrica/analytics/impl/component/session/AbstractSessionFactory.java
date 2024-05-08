package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSessionFactory implements ISessionFactory<SessionArguments> {

    private static final String TAG = "[AbstractSessionFactory]";

    private final ComponentUnit mComponentUnit;
    @NonNull
    private final SessionIDProvider sessionIDProvider;
    private final SessionStorageImpl mStorage;
    private final SessionFactoryArguments mArguments;
    @NonNull
    private final IReporterExtended mSelfReporter;
    @NonNull
    private final SystemTimeProvider timeProvider;

    public AbstractSessionFactory(
            @NonNull ComponentUnit componentUnit,
            @NonNull SessionIDProvider sessionIDProvider,
            @NonNull SessionStorageImpl storage,
            @NonNull SessionFactoryArguments arguments,
            @NonNull IReporterExtended selfReporter,
            @NonNull SystemTimeProvider timeProvider) {
        mComponentUnit = componentUnit;
        this.sessionIDProvider = sessionIDProvider;
        mStorage = storage;
        mArguments = arguments;
        mSelfReporter = selfReporter;
        this.timeProvider = timeProvider;
    }

    @Nullable
    public final Session load() {
        if (mStorage.hasValues()) {
            return new Session(mComponentUnit, mStorage, fillFromStorage(), timeProvider);
        } else {
            return null;
        }
    }

    @NonNull
    public final Session create(@NonNull SessionArguments arguments) {
        if (mStorage.hasValues()) {
            mSelfReporter.reportEvent("create session with non-empty storage");
        }
        return new Session(mComponentUnit, mStorage, createNewSession(arguments));
    }

    @NonNull
    private SessionArgumentsInternal createNewSession(@NonNull SessionArguments arguments) {

        long sessionId = sessionIDProvider.getNextSessionId();
        mStorage.putSessionId(sessionId)
                .putSleepStart(arguments.creationElapsedRealtime)
                .putCreationTime(arguments.creationElapsedRealtime)
                .putReportId(SessionDefaults.INITIAL_REPORT_ID)
                .putAliveReportNeeded(true)
                .commit();

        mComponentUnit.getDbHelper().newSession(
                sessionId,
                mArguments.getType(),
                TimeUnit.MILLISECONDS.toSeconds(arguments.creationTimestamp)
        );

        YLogger.debug(TAG, "%s session created: %s", mArguments.getType(), sessionId);
        return fillFromStorage();
    }

    @VisibleForTesting
    @NonNull
    SessionArgumentsInternal fillFromStorage() {
        return SessionArgumentsInternal.newBuilder(mArguments)
                .withAliveNeeded(mStorage.isAliveReportNeeded())
                .withCurrentReportId(mStorage.getReportId())
                .withCreationTime(mStorage.getCreationTime())
                .withId(mStorage.getSessionId())
                .withSleepStart(mStorage.getSleepStart())
                .withLastEventOffset(mStorage.getLastEventOffset())
                .build();
    }

}
