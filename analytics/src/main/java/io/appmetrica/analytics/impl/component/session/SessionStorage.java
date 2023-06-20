package io.appmetrica.analytics.impl.component.session;

public interface SessionStorage {

    SessionStorageImpl putSessionId(final long value);

    SessionStorageImpl putCreationTime(final long value);

    SessionStorageImpl putReportId(final long value);

    SessionStorageImpl putSleepStart(final long value);

    SessionStorageImpl putLastEventOffset(long value);

    SessionStorageImpl putAliveReportNeeded(final boolean value);

    void commit();

    void clear();

}
