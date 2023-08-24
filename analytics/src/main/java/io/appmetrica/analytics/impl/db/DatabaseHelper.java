package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.db.DBUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventNumberGenerator;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.event.DbEventModel;
import io.appmetrica.analytics.impl.db.event.DbEventModelFactory;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter;
import io.appmetrica.analytics.impl.db.session.DbSessionModelFactory;
import io.appmetrica.analytics.impl.events.EventListener;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.encryption.EncryptedCounterReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.appmetrica.analytics.impl.db.constants.Constants.EventsTable.EventTableEntry;
import static io.appmetrica.analytics.impl.db.constants.Constants.SessionTable;
import static io.appmetrica.analytics.impl.db.constants.Constants.SessionTable.SessionTableEntry;

public class DatabaseHelper {

    private static final String TAG = "[DbHelper] ";

    private static final HashSet<Integer> NOT_THROTTLED_EVENTS = new HashSet<Integer>();

    static {
        NOT_THROTTLED_EVENTS.add(InternalEvents.EVENT_TYPE_INIT.getTypeId());
        NOT_THROTTLED_EVENTS.add(InternalEvents.EVENT_TYPE_START.getTypeId());
    }

    // It will treat the locking of the database access, avoiding conflicts.
    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();
    private final Lock mReadLock = mLock.readLock();
    private final Lock mWriteLock = mLock.writeLock();
    private final DatabaseStorage mStorage;

    // Worker that helps us to work with the database - safely.
    private final WorkerThread mDbWorker;

    private final Object mEventQueueMonitor = new Object();
    private final List<ContentValues> mEventQueue = new ArrayList<ContentValues>(3);

    private final Context mContext;
    private final ComponentUnit mComponent;
    private final AtomicLong mRowCount = new AtomicLong();
    @NonNull
    private final List<EventListener> mEventListeners = new ArrayList<EventListener>();
    @NonNull
    private final DatabaseCleaner mDatabaseCleaner;
    @NonNull
    private final DbEventModelConverter dbEventModelConverter;

    public DatabaseHelper(@NonNull ComponentUnit component, final DatabaseStorage databaseStorage) {
        this(
            component,
            databaseStorage,
            new DatabaseCleaner(component.getReporterType()),
            new DbEventModelConverter()
        );
    }

    public DatabaseHelper(@NonNull ComponentUnit component,
                          final DatabaseStorage databaseStorage,
                          @NonNull DatabaseCleaner databaseCleaner,
                          @NonNull final DbEventModelConverter dbEventModelConverter) {
        mStorage = databaseStorage;
        mContext = component.getContext();
        mComponent = component;
        mDatabaseCleaner = databaseCleaner;
        this.dbEventModelConverter = dbEventModelConverter;

        mRowCount.set(getRowCountFromDb());

        mDbWorker = new WorkerThread(component);
        mDbWorker.setName(formDbWorkerName(component));
    }

    public void onComponentCreated() {
        mDbWorker.start();
    }

    public long getEventsCount() {
        mReadLock.lock();
        try {
            return mRowCount.get();
        } finally {
            mReadLock.unlock();
        }
    }

    public long getEventsOfFollowingTypesCount(@NonNull Set<Integer> types) {
        mReadLock.lock();
        Cursor cursor = null;
        long rowsCount = 0;
        try {
            SQLiteDatabase db = mStorage.getReadableDatabase();
            if (db != null) {
                StringBuilder query = new StringBuilder("SELECT count() FROM " + Constants.EventsTable.TABLE_NAME);
                if (types.isEmpty() == false) {
                    query.append(" WHERE ");
                }
                int i = 0;
                for (Integer type : types) {
                    if (i > 0) {
                        query.append(" OR ");
                    }
                    query.append(EventTableEntry.FIELD_EVENT_TYPE + " == " + type);
                    i++;
                }
                cursor = db.rawQuery(query.toString(), null);
                if (cursor.moveToFirst()) {
                    rowsCount = cursor.getLong(0);
                }
            }
        } catch (Throwable ex) {
            YLogger.e(TAG + "Smth was wrong counting reports.\n%s", ex);
        } finally {
            Utils.closeCursor(cursor);
            mReadLock.unlock();
        }
        return rowsCount;
    }

    public void addEventListener(@NonNull EventListener eventListener) {
        mEventListeners.add(eventListener);
    }

    private static String formDbWorkerName(final IComponent component) {
        return WorkerThread.PREFIX_THREAD_NAME + " [" + component.getComponentId().toStringAnonymized() + "]";
    }

    // Creates and writes a new session by Id
    public void newSession(final long sessionId, final SessionType type, final long sessionStartTime) {
        YLogger.d(TAG + "New session was created. Session Id: %d", sessionId);

        final ContentValues sessionValues = new DbSessionModelConverter().fromModel(
            new DbSessionModelFactory(
                mComponent.getFreshReportRequestConfig(),
                sessionId,
                type,
                sessionStartTime
            ).create()
        );

        addSessionValues(sessionValues);
    }

    public void saveReport(@NonNull final EncryptedCounterReport reportData,
                           final int reportType,
                           @NonNull final SessionState sessionState,
                           @NonNull final AppEnvironment.EnvironmentRevision environmentRevision,
                           @NonNull final EventNumberGenerator eventNumberGenerator) {
        final DbEventModel dbEventModel = new DbEventModelFactory(
            mContext,
            sessionState,
            reportType,
            eventNumberGenerator,
            reportData,
            mComponent.getFreshReportRequestConfig(),
            environmentRevision
        ).create();
        final ContentValues reportValues = dbEventModelConverter.fromModel(dbEventModel);
        YLogger.i(TAG + "A new report: %s", StringUtils.contentValuesToString(reportValues));
        addReportValues(reportValues);
    }

    private long getRowCountFromDb() {
        long rowCount = 0;
        mReadLock.lock();
        try {
            final SQLiteDatabase db = mStorage.getReadableDatabase();
            if (db != null) {
                rowCount = DBUtils.queryRowsCount(db, Constants.EventsTable.TABLE_NAME);
            }
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        } finally {
            mReadLock.unlock();
        };
        return rowCount;
    }

    public void addSessionValues(final ContentValues sessionValues) {
        insertSession(sessionValues);
    }

    public void addReportValues(final ContentValues reportValues) {
        synchronized (mEventQueueMonitor) {
            mEventQueue.add(reportValues);
        }

        synchronized (mDbWorker) {
            mDbWorker.notifyAll();
        }
    }

    public int removeEmptySessions(long thresholdSessionId) {
        int affectedRows = 0;
        mWriteLock.lock();
        try {
            if (Constants.PROFILE_SQL) {
                logSessionInfo();
            }

            final SQLiteDatabase wDatabase = mStorage.getWritableDatabase();
            if (wDatabase != null) {
                YLogger.d("%s Try to remove empty sessions with id less than %d", TAG, thresholdSessionId);
                affectedRows = wDatabase.delete(SessionTable.TABLE_NAME, SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS,
                        new String[]{String.valueOf(thresholdSessionId)});

                YLogger.i(TAG + "Removed empty sessions - affected: %d", affectedRows);
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while removing session.\n%s", exception);
        } finally {
            mWriteLock.unlock();
        }
        return affectedRows;
    }

    //For debugging only
    private void logSessionInfo() {
        Cursor allSessionsCursor = null;
        Cursor sessionsInReportsCursor = null;
        mReadLock.lock();
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                allSessionsCursor = rDatabase.rawQuery(SessionTable.ALL_SESSION, new String[]{});
                StringBuffer buf = new StringBuffer();
                buf.append("All sessions in db: ");
                while (allSessionsCursor.moveToNext()) {
                    buf.append(allSessionsCursor.getString(0)).append(", ");
                }
                YLogger.i(TAG + buf.toString());

                sessionsInReportsCursor = rDatabase.rawQuery(SessionTable.ALL_SESSION_IN_REPORTS, new String[]{});
                StringBuffer buffer = new StringBuffer();
                buffer.append("All sessions in reports db: ");
                while (sessionsInReportsCursor.moveToNext()) {
                    buffer.append(sessionsInReportsCursor.getString(0)).append(", ");
                }
                YLogger.i(TAG + buffer.toString());
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while logging sessions.\n%s", exception);
        } finally {
            mReadLock.unlock();
            Utils.closeCursor(allSessionsCursor);
            Utils.closeCursor(sessionsInReportsCursor);
        }
    }

    public void clearIfTooManyEvents() {
        try {
            mWriteLock.lock();
            final long maxReportsInDbCount = mComponent.getFreshReportRequestConfig().getMaxEventsInDbCount();
            final long currentRowCount = mRowCount.get();
            YLogger.d("Should clear db? Current reports count: %d, max: %d", currentRowCount, maxReportsInDbCount);
            if (currentRowCount > maxReportsInDbCount) {
                YLogger.i("%sTrying to clear reports table. Row count: %s, rows count: %s, max: %s",
                        TAG, currentRowCount, mRowCount, maxReportsInDbCount);
                SQLiteDatabase db = mStorage.getWritableDatabase();
                if (db != null) {
                    int deletedRowsCount = deleteExcessiveReports(db);
                    long allRowsCount = mRowCount.addAndGet(-deletedRowsCount);
                    YLogger.i("%sReports table cleared. %d rows deleted. Row count: %d",
                            TAG, deletedRowsCount, allRowsCount);
                }
            }
        } catch (Throwable e) {
            YLogger.e(e, TAG + "Smth was wrong while clearing database.");
        } finally {
            mWriteLock.unlock();
        }
    }

    private int deleteExcessiveReports(final SQLiteDatabase db) {
        try {
            int percentToDelete = 10;
            String whereClause = String.format(Constants.EventsTable.DELETE_EXCESSIVE_RECORDS_WHERE,
                    TextUtils.join(", ", EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY),
                    TextUtils.join(", ", EventsManager.EVENTS_WITH_SECOND_HIGHEST_PRIORITY),
                    percentToDelete
            );
            return mDatabaseCleaner.cleanEvents(
                    db,
                    Constants.EventsTable.TABLE_NAME,
                    whereClause,
                    DatabaseCleaner.Reason.DB_OVERFLOW,
                    mComponent.getComponentId().getApiKey(),
                    true
            ).mDeletedRowsCount;
        } catch (Throwable e) {
            YLogger.e(e, "%sSomething was wrong while removing excessive reports from db", TAG);
            AppMetricaSelfReportFacade.getReporter()
                    .reportError("deleteExcessiveReports exception", e);
            return 0;
        }
    }

    // Removes session's reports inside the database by session ID and limit of records.
    public void removeTop(final long sessionId,
                          final int sessionType,
                          final int limitOfRecords,
                          final boolean shouldFormCleanupEvent) throws SQLiteException {
        if (limitOfRecords <= 0) {
            return;
        }

        mWriteLock.lock();
        try {
            final String deleteQuery = String.format(Locale.US,
                Constants.EventsTable.DELETE_TOP_RECORDS_WHERE,
                EventTableEntry.FIELD_EVENT_SESSION, Long.toString(sessionId),
                EventTableEntry.FIELD_EVENT_SESSION_TYPE, Integer.toString(sessionType),
                EventTableEntry.FIELD_EVENT_ID, Constants.EventsTable.TABLE_NAME,
                Integer.toString(limitOfRecords - 1)
            );

            final SQLiteDatabase db = mStorage.getWritableDatabase();
            if (db != null) {
                DatabaseCleaner.DeletionInfo deletionInfo = mDatabaseCleaner.cleanEvents(
                        db,
                        Constants.EventsTable.TABLE_NAME,
                        deleteQuery,
                        DatabaseCleaner.Reason.BAD_REQUEST,
                        mComponent.getComponentId().getApiKey(),
                        shouldFormCleanupEvent
                );

                if (deletionInfo.selectedEvents != null) {
                    List<Integer> reportTypes = new ArrayList<Integer>();
                    for (ContentValues report : deletionInfo.selectedEvents) {
                        reportTypes.add(getReportType(report));
                    }
                    for (EventListener listener : mEventListeners) {
                        listener.onEventsRemoved(reportTypes);
                    }
                }

                if (mComponent.getPublicLogger().isEnabled() && deletionInfo.selectedEvents != null) {
                    logEvents(deletionInfo.selectedEvents, "Event removed from db");
                }
                final int deletedRows = deletionInfo.mDeletedRowsCount;
                long rows = mRowCount.addAndGet(-deletedRows);
                YLogger.d(TAG + "%d reports removed. Row count %d", deletedRows, rows);
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while removing top records for session.\n%s", exception);
        } finally {
            mWriteLock.unlock();
        }
    }

    // Queries all sessions from the database without special sessions.
    @Nullable
    public Cursor querySessions(final Map<String, String> extraSelection) {
        Cursor dataCursor = null;

        mReadLock.lock();
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                dataCursor = rDatabase.query(
                        SessionTable.TABLE_NAME, null,
                        formWhereClause(SessionTableEntry.FIELD_SESSION_ID + " >= ?", extraSelection),
                        formWhereArgs(new String [] {Long.toString(0)}, extraSelection),
                        null, null, SessionTableEntry.FIELD_SESSION_ID + " ASC", null
                );
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while querying sessions.\n%s", exception);
        } finally {
            mReadLock.unlock();
        }

        return dataCursor;
    }

    // Queries a session by ID.
    public Cursor querySession(final long sessionId, final Map<String, String> extraSelection) {
        Cursor dataCursor = null;

        mReadLock.lock();
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                dataCursor = rDatabase.query(
                        SessionTable.TABLE_NAME, null,
                        formWhereClause(SessionTableEntry.FIELD_SESSION_ID + " = ?", extraSelection),
                        formWhereArgs(new String [] {Long.toString(sessionId)}, extraSelection),
                        null, null, null, null
                );
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while querying session by Id.\n%s", exception);
        } finally {
            mReadLock.unlock();
        }

        return dataCursor;
    }

    // Queries reports by session ID.
    @Nullable
    public Cursor queryReports(final long sessionId, @NonNull final SessionType sessionType) throws SQLiteException {
        Cursor dataCursor = null;

        mReadLock.lock();
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                dataCursor = rDatabase.query(
                        Constants.EventsTable.TABLE_NAME, null,
                        Constants.EventsTable.SELECT_BY_SESSION_WHERE,
                        new String [] {Long.toString(sessionId), Integer.toString(sessionType.getCode())},
                        null, null, EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION + " ASC", null
                );
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while querying reports by session Id.\n%s", exception);
        } finally {
            mReadLock.unlock();
        }

        return dataCursor;
    }

    private void insertSession(final ContentValues session) {
        if (null == session) {
            return;
        }

        mWriteLock.lock();
        try {
            final SQLiteDatabase wDatabase = mStorage.getWritableDatabase();
            if (wDatabase != null) {
                wDatabase.insertOrThrow(SessionTable.TABLE_NAME, null, session);
                YLogger.d("session saved %s", session);
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while inserting some session into database.\n%s", exception);
        } finally {
            mWriteLock.unlock();
        }
    }

    @VisibleForTesting
    void insertEvents(final List<ContentValues> reports) {
        if (null == reports || reports.isEmpty()) {
            return;
        }

        SQLiteDatabase wDatabase = null;
        mWriteLock.lock();
        try {
            wDatabase = mStorage.getWritableDatabase();

            if (wDatabase != null) {
                wDatabase.beginTransaction();

                for (final ContentValues reportItem : reports) {
                    wDatabase.insertOrThrow(Constants.EventsTable.TABLE_NAME, null, reportItem);
                    mRowCount.incrementAndGet();
                    logEvent(reportItem, "Event saved to db");
                }

                wDatabase.setTransactionSuccessful();
                long rows = mRowCount.get();
                YLogger.d(TAG + "report saved. Row count %d", rows);
            }
        } catch (Throwable exception) {
            YLogger.e(TAG + "Smth was wrong while inserting reports into database.\n%s", exception);
        } finally {
            Utils.endTransaction(wDatabase);
            mWriteLock.unlock();
        }
    }

    private void logEvent(final ContentValues reportItem, final String msg) {
        if (EventsManager.isPublicForLogs(getReportTypeFromContentValues(reportItem))) {
            final DbEventModel dbEventModel = new DbEventModelConverter().toModel(reportItem);
            StringBuilder logMessage = new StringBuilder(msg);
            logMessage.append(": ");
            logMessage.append(dbEventModel.getDescription().getName());
            String value = dbEventModel.getDescription().getValue();
            if (EventsManager.shouldLogValue(getReportType(reportItem)) && TextUtils.isEmpty(value) == false) {
                logMessage.append(" with value ");
                logMessage.append(value);
            }
            mComponent.getPublicLogger().i(logMessage.toString());
        }
    }

    private void logEvents(final List<ContentValues> reports, final String msg) {
        for (int i = 0; i < reports.size(); i++) {
            logEvent(reports.get(i), msg);
        }
    }

    // Collects all unique query parameters
    @NonNull
    public List<ContentValues> collectAllQueryParameters() {
        List<ContentValues> queryParameters = new ArrayList<ContentValues>();

        mReadLock.lock();
        Cursor dataCursor = null;
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                // Now it's just report request parameters column
                String query = SessionTable.DISTINCT_REPORT_REQUEST_PARAMETERS;

                dataCursor = rDatabase.rawQuery(query, null);
                while (dataCursor.moveToNext()) {
                    final ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(dataCursor, values);
                    queryParameters.add(values);
                }
            }
        } catch (Throwable exception) {
            queryParameters = new ArrayList<ContentValues>();
            YLogger.e(TAG + "Smth was wrong while collecting all query parameters.\n%s", exception);
        } finally {
            Utils.closeCursor(dataCursor);
            mReadLock.unlock();
        }

        return queryParameters;
    }

    public ContentValues getSessionRequestParameters(final long sessionId, SessionType sessionType) {
        ContentValues queryParameters = new ContentValues();

        mReadLock.lock();
        Cursor dataCursor = null;
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                String query = String.format(Locale.US, SessionTable.QUERY_GET_SESSION_REQUEST_PARAMETERS, sessionId,
                        sessionType.getCode());
                dataCursor = rDatabase.rawQuery(query, null);
                if (dataCursor.moveToNext()) {
                    final ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(dataCursor, values);
                    queryParameters = values;
                }
            }
        } catch (Throwable exception) {
            YLogger.e(exception, "Smth was wrong while getting query parameters for session = %s", sessionId);
        } finally {
            Utils.closeCursor(dataCursor);
            mReadLock.unlock();
        }

        return queryParameters;
    }

    private static String formWhereClause(final String preWhereClause, final Map<String, String> extraSelection) {
        final StringBuilder whereClause = new StringBuilder(preWhereClause);

        for (final String arg : extraSelection.keySet()) {
            whereClause.append(whereClause.length() > 0 ? " AND " : "");
            whereClause.append(arg + " = ? ");
        }

        return TextUtils.isEmpty(whereClause.toString()) ? null : whereClause.toString();
    }

    private static String [] formWhereArgs(final String [] preWhereArgs, final Map<String, String> extraSelection) {
        final List<String> whereArgs = new ArrayList<String>();
        whereArgs.addAll(Arrays.asList(preWhereArgs));

        for (final Map.Entry<String, String> selectionEntry : extraSelection.entrySet()) {
            whereArgs.add(selectionEntry.getValue());
        }

        return whereArgs.toArray(new String [whereArgs.size()]);
    }

    private static int getReportTypeFromContentValues(final ContentValues values) {
        final Integer eventTypeId = values.getAsInteger(EventTableEntry.FIELD_EVENT_TYPE);
        return eventTypeId != null ? eventTypeId : -1;
    }

    private int getReportType(ContentValues reportItem) {
        return getReportIntField(reportItem, EventTableEntry.FIELD_EVENT_TYPE);
    }

    private int getReportIntField(ContentValues reportItem, String fieldName) {
        return reportItem.getAsInteger(fieldName);
    }

    private class WorkerThread extends InterruptionSafeThread {

        static final String PREFIX_THREAD_NAME = "DatabaseWorker";

        @NonNull
        private final ComponentUnit mComponentUnit;

        WorkerThread(@NonNull ComponentUnit component) {
            super();
            mComponentUnit = component;
        }

        @Override
        public void run() {
            while (isRunning()) {
                try {
                    synchronized (this) {
                        if (isNoTasks()) {
                            wait();
                        }
                    }
                } catch (Throwable e) {
                    stopRunning();
                }

                List<ContentValues> buffer;
                synchronized (mEventQueueMonitor) {
                    buffer = new ArrayList<ContentValues>(mEventQueue);
                    mEventQueue.clear();
                }
                insertEvents(buffer);

                //todo (avitenko) workaround. Will be changed in METRIKALIB-2395
                notifyListeners(buffer);
            }
        }

        synchronized void notifyListeners(@NonNull List<ContentValues> reports) {
            List<Integer> reportTypes = new ArrayList<Integer>();
            for (ContentValues report : reports) {
                reportTypes.add(getReportType(report));
            }
            for (EventListener listener : mEventListeners) {
                listener.onEventsAdded(reportTypes);
            }
            mComponentUnit.getEventTrigger().trigger();
        }

    }

    private boolean isNoTasks() {
        synchronized (mEventQueueMonitor) {
            return mEventQueue.isEmpty();
        }
    }
}
