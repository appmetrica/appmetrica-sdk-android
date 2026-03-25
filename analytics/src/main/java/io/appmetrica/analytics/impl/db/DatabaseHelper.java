package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.db.DBUtils;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionRequestParams;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.db.constants.Constants;

import static io.appmetrica.analytics.impl.db.constants.Constants.EventsTable.EventTableEntry;
import static io.appmetrica.analytics.impl.db.constants.Constants.SessionTable;
import static io.appmetrica.analytics.impl.db.constants.Constants.SessionTable.SessionTableEntry;
import io.appmetrica.analytics.impl.db.event.DbEventModel;
import io.appmetrica.analytics.impl.db.event.DbEventModelFactory;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter;
import io.appmetrica.analytics.impl.db.session.DbSessionModelFactory;
import io.appmetrica.analytics.impl.events.EventListener;
import io.appmetrica.analytics.impl.events.UrgentEvents;
import io.appmetrica.analytics.impl.utils.PublicLogConstructor;
import io.appmetrica.analytics.impl.utils.encryption.EncryptedCounterReport;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.json.JSONObject;

public class DatabaseHelper {

    private static final String TAG = "[DbHelper] ";

    // It will treat the locking of the database access, avoiding conflicts.
    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();
    private final Lock mReadLock = mLock.readLock();
    private final Lock mWriteLock = mLock.writeLock();
    private final DatabaseStorage mStorage;

    private final Context mContext;
    private final ComponentUnit mComponent;
    private final AtomicLong mRowCount = new AtomicLong();
    @NonNull
    private final List<EventListener> mEventListeners = new ArrayList<EventListener>();
    @NonNull
    private final DatabaseCleaner mDatabaseCleaner;
    @NonNull
    private final DbEventModelConverter dbEventModelConverter;
    @NonNull
    private final EventBatchWriter eventBatchWriter;
    @NonNull
    private final BufferedEventsWriter bufferedEventsWriter;

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

        this.eventBatchWriter = new EventBatchWriter(
            mStorage,
            mComponent,
            mRowCount,
            mEventListeners,
            mDatabaseCleaner,
            mLock
        );

        this.bufferedEventsWriter = new BufferedEventsWriter(
            this.eventBatchWriter,
            GlobalServiceLocator.getInstance().getServiceExecutorProvider().getPersistenceExecutor(),
            1000 // 1 second delay
        );
    }

    public void onComponentCreated() {
        // All components initialized, nothing to do here
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
        long rowsCount = 0;
        try {
            SQLiteDatabase db = mStorage.getReadableDatabase();
            if (db != null) {
                String selection = null;
                String[] selectionArgs = null;
                if (!types.isEmpty()) {
                    StringBuilder selectionBuilder = new StringBuilder(
                        EventTableEntry.FIELD_EVENT_TYPE + " IN ("
                    );
                    selectionArgs = new String[types.size()];
                    int i = 0;
                    for (Integer type : types) {
                        if (i > 0) selectionBuilder.append(", ");
                        selectionBuilder.append("?");
                        selectionArgs[i++] = String.valueOf(type);
                    }
                    selectionBuilder.append(")");
                    selection = selectionBuilder.toString();
                }
                rowsCount = DatabaseUtils.queryNumEntries(
                    db,
                    Constants.EventsTable.TABLE_NAME,
                    selection,
                    selectionArgs
                );
            }
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, "Smth was wrong counting reports.\n%s", ex);
        } finally {
            mReadLock.unlock();
        }
        return rowsCount;
    }

    public void addEventListener(@NonNull EventListener eventListener) {
        mEventListeners.add(eventListener);
    }

    /**
     * Flushes all pending events for this DatabaseHelper asynchronously.
     * This ensures all buffered events are written to the database.
     */
    public void flushAsync() {
        DebugLogger.INSTANCE.info(TAG, "flushAsync called for DatabaseHelper");
        bufferedEventsWriter.flushAsync();
    }

    // Creates and writes a new session by Id
    public void newSession(final long sessionId, final SessionType type, final long sessionStartTimeSeconds) {
        DebugLogger.INSTANCE.info(TAG, "New session was created. Session Id: %d", sessionId);

        final ContentValues sessionValues = new DbSessionModelConverter().fromModel(
            new DbSessionModelFactory(
                mComponent.getFreshReportRequestConfig(),
                sessionId,
                type,
                sessionStartTimeSeconds
            ).create()
        );

        addSessionValues(sessionValues);
    }

    // Creates and writes a new session by Id using request params from a past session.
    // It is only used to create a session for crashes.
    public void newSessionFromPast(
        final long sessionId,
        final SessionType type,
        final long sessionStartTimeSeconds,
        @Nullable final SessionRequestParams sessionRequestParams
    ) {
        DebugLogger.INSTANCE.info(TAG, "New session from past was created. Session Id: %d", sessionId);

        final ContentValues sessionValues = new DbSessionModelConverter().fromModel(
            new DbSessionModelFactory(
                sessionRequestParams,
                mComponent.getFreshReportRequestConfig(),
                sessionId,
                type,
                sessionStartTimeSeconds
            ).create()
        );

        addSessionValues(sessionValues);
    }

    public void saveReport(@NonNull final EncryptedCounterReport reportData,
                           final int reportType,
                           @NonNull final SessionState sessionState,
                           @NonNull final AppEnvironment.EnvironmentRevision environmentRevision,
                           @NonNull final VitalComponentDataProvider vitalComponentDataProvider) {
        final DbEventModel dbEventModel = new DbEventModelFactory(
            mContext,
            sessionState,
            reportType,
            vitalComponentDataProvider,
            reportData,
            mComponent.getFreshReportRequestConfig(),
            environmentRevision
        ).create();
        final ContentValues reportValues = dbEventModelConverter.fromModel(dbEventModel);
        DebugLogger.INSTANCE.info(
            TAG,
            "A new report: %s",
            StringUtils.contentValuesToString(reportValues)
        );
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
            DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
        } finally {
            mReadLock.unlock();
        };
        return rowCount;
    }

    public void addSessionValues(final ContentValues sessionValues) {
        insertSession(sessionValues);
    }

    public void addReportValues(final ContentValues reportValues) {
        DebugLogger.INSTANCE.info(TAG, "add report values: %s", reportValues);

        boolean isUrgentEvent = UrgentEvents.isUrgent(getReportType(reportValues));

        DebugLogger.INSTANCE.info(
            TAG,
            "Add report values (urgent: %s, type: %d)",
            isUrgentEvent,
            getReportType(reportValues)
        );

        bufferedEventsWriter.addEvent(reportValues, isUrgentEvent);
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
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Try to remove empty sessions with id less than %d",
                    thresholdSessionId
                );
                affectedRows = wDatabase.delete(SessionTable.TABLE_NAME, SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS,
                        new String[]{String.valueOf(thresholdSessionId)});

                DebugLogger.INSTANCE.info(
                    TAG,
                    "Removed empty sessions - affected: %d",
                    affectedRows
                );
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(TAG, "Smth was wrong while removing session.\n%s", exception);
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
                allSessionsCursor = rDatabase.query(
                    true, SessionTable.TABLE_NAME,
                    new String[]{SessionTableEntry.FIELD_SESSION_ID},
                    null, null, null, null,
                    SessionTableEntry.FIELD_SESSION_ID + " ASC", null
                );
                StringBuffer buf = new StringBuffer();
                buf.append("All sessions in db: ");
                while (allSessionsCursor.moveToNext()) {
                    buf.append(allSessionsCursor.getString(0)).append(", ");
                }
                DebugLogger.INSTANCE.info(TAG, buf.toString());

                sessionsInReportsCursor = rDatabase.query(
                    true, Constants.EventsTable.TABLE_NAME,
                    new String[]{EventTableEntry.FIELD_EVENT_SESSION},
                    null, null, null, null,
                    EventTableEntry.FIELD_EVENT_SESSION + " ASC", null
                );
                StringBuffer buffer = new StringBuffer();
                buffer.append("All sessions in reports db: ");
                while (sessionsInReportsCursor.moveToNext()) {
                    buffer.append(sessionsInReportsCursor.getString(0)).append(", ");
                }
                DebugLogger.INSTANCE.info(TAG, buffer.toString());
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(TAG, "Smth was wrong while logging sessions.\n%s", exception);
        } finally {
            mReadLock.unlock();
            Utils.closeCursor(allSessionsCursor);
            Utils.closeCursor(sessionsInReportsCursor);
        }
    }

    public void removeSessionsEventsUpTo(
        @NonNull List<SessionEventsDeleteParams> paramsList,
        long thresholdSessionId
    ) {
        processCleanupResults(executeCleanupTransaction(paramsList, thresholdSessionId));
    }

    @NonNull
    private List<DatabaseCleaner.DeletionInfo> executeCleanupTransaction(
        @NonNull List<SessionEventsDeleteParams> paramsList,
        long thresholdSessionId
    ) {
        final List<DatabaseCleaner.DeletionInfo> deletionInfos = new ArrayList<>();
        final String deleteQuery = buildDeleteQuery();
        mWriteLock.lock();
        try {
            final SQLiteDatabase db = mStorage.getWritableDatabase();
            if (db != null) {
                if (Constants.PROFILE_SQL) {
                    logSessionInfo();
                }
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Try to remove empty sessions with id less than %d",
                    thresholdSessionId
                );
                db.beginTransaction();
                try {
                    for (SessionEventsDeleteParams params : paramsList) {
                        deletionInfos.add(cleanSessionEvents(db, deleteQuery, params));
                    }
                    final int removedSessionsCount = db.delete(
                        SessionTable.TABLE_NAME,
                        SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS,
                        new String[]{String.valueOf(thresholdSessionId)}
                    );
                    db.setTransactionSuccessful();
                    DebugLogger.INSTANCE.info(TAG, "Removed empty sessions - affected: %d", removedSessionsCount);
                } finally {
                    db.endTransaction();
                }
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                "Smth was wrong while removing session events from db.\n%s",
                exception
            );
        } finally {
            mWriteLock.unlock();
        }
        return deletionInfos;
    }

    private void processCleanupResults(@NonNull List<DatabaseCleaner.DeletionInfo> deletionInfos) {
        int totalDeletedRows = 0;
        for (DatabaseCleaner.DeletionInfo deletionInfo : deletionInfos) {
            notifyEventsRemoved(deletionInfo);
            totalDeletedRows += deletionInfo.mDeletedRowsCount;
        }
        long rows = mRowCount.addAndGet(-totalDeletedRows);
        DebugLogger.INSTANCE.info(TAG, "%d reports removed. Row count %d", totalDeletedRows, rows);
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
            DebugLogger.INSTANCE.error(TAG, "Smth was wrong while querying sessions.\n%s", exception);
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
            DebugLogger.INSTANCE.error(TAG, "Smth was wrong while querying session by Id.\n%s", exception);
        } finally {
            mReadLock.unlock();
        }

        return dataCursor;
    }

    // Queries reports for multiple sessions at once, fetching at most {@code limit} rows.
    @Nullable
    public Cursor queryReportsForSessions(@NonNull Map<Long, Integer> sessionIdToTypeCode, int limit) {
        if (sessionIdToTypeCode.isEmpty()) {
            return null;
        }
        Cursor dataCursor = null;
        mReadLock.lock();
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                final StringBuilder selection = new StringBuilder();
                final String[] selectionArgs = new String[sessionIdToTypeCode.size() * 2];
                int i = 0;
                for (Map.Entry<Long, Integer> entry : sessionIdToTypeCode.entrySet()) {
                    if (i > 0) {
                        selection.append(" OR ");
                    }
                    selection.append("(")
                        .append(EventTableEntry.FIELD_EVENT_SESSION).append(" = ? AND ")
                        .append(EventTableEntry.FIELD_EVENT_SESSION_TYPE).append(" = ?)");
                    selectionArgs[i * 2] = Long.toString(entry.getKey());
                    selectionArgs[i * 2 + 1] = Integer.toString(entry.getValue());
                    i++;
                }
                dataCursor = rDatabase.query(
                    Constants.EventsTable.TABLE_NAME, null,
                    selection.toString(), selectionArgs, null, null,
                    EventTableEntry.FIELD_EVENT_SESSION + " ASC, "
                        + EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION + " ASC",
                    String.valueOf(limit)
                );
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                "Smth was wrong while querying reports for sessions.\n%s",
                exception
            );
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
                DebugLogger.INSTANCE.info(TAG, "session saved %s", session);
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                "Smth was wrong while inserting some session into database.\n%s",
                exception
            );
        } finally {
            mWriteLock.unlock();
        }
    }

    @VisibleForTesting
    @NonNull
    EventBatchWriter getEventBatchWriter() {
        return eventBatchWriter;
    }

    /**
     * For testing only: synchronously inserts events without buffering.
     * @deprecated Use addReportValues() instead
     */
    @VisibleForTesting
    @Deprecated
    void insertEvents(final List<ContentValues> reports) {
        eventBatchWriter.writeEvents(reports);
    }

    private void logEvent(final ContentValues reportItem, final String msg) {
        if (EventsManager.isPublicForLogs(getReportTypeFromContentValues(reportItem))) {
            final DbEventModel dbEventModel = new DbEventModelConverter().toModel(reportItem);
            mComponent.getPublicLogger().info(
                PublicLogConstructor.constructLogValueForInternalEvent(
                    msg,
                    dbEventModel.getType(),
                    dbEventModel.getDescription().getName(),
                    dbEventModel.getDescription().getValue()
                )
            );
        }
    }

    private DatabaseCleaner.DeletionInfo cleanSessionEvents(
        @NonNull SQLiteDatabase db,
        @NonNull String deleteQuery,
        @NonNull SessionEventsDeleteParams params
    ) {
        return mDatabaseCleaner.cleanEvents(
            db,
            Constants.EventsTable.TABLE_NAME,
            deleteQuery,
            new String[]{
                Long.toString(params.getSessionId()),
                Integer.toString(params.getSessionType()),
                Long.toString(params.getMaxNumberInSession())
            },
            DatabaseCleaner.Reason.BAD_REQUEST,
            mComponent.getComponentId().getApiKey(),
            params.getShouldFormCleanupEvent()
        );
    }

    private String buildDeleteQuery() {
        return String.format(Locale.US,
            Constants.EventsTable.DELETE_RECORDS_UP_TO_NUMBER_IN_SESSION,
            EventTableEntry.FIELD_EVENT_SESSION,
            EventTableEntry.FIELD_EVENT_SESSION_TYPE,
            EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION
        );
    }

    private void notifyEventsRemoved(@NonNull DatabaseCleaner.DeletionInfo deletionInfo) {
        if (deletionInfo.selectedEvents != null) {
            List<Integer> reportTypes = new ArrayList<>();
            for (ContentValues report : deletionInfo.selectedEvents) {
                reportTypes.add(getReportType(report));
            }
            for (EventListener listener : mEventListeners) {
                listener.onEventsRemoved(reportTypes);
            }
            logEvents(deletionInfo.selectedEvents, "Event removed from db");
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
                dataCursor = rDatabase.rawQuery(SessionTable.DISTINCT_REPORT_REQUEST_PARAMETERS, null);
                while (dataCursor.moveToNext()) {
                    final ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(dataCursor, values);
                    queryParameters.add(values);
                }
            }
        } catch (Throwable exception) {
            queryParameters = new ArrayList<ContentValues>();
            DebugLogger.INSTANCE.error(
                TAG,
                "Smth was wrong while collecting all query parameters.\n%s",
                exception
            );
        } finally {
            Utils.closeCursor(dataCursor);
            mReadLock.unlock();
        }

        return queryParameters;
    }

    public SessionRequestParams getSessionRequestParams(final long sessionId, SessionType sessionType) {
        ContentValues params = getSessionRequestParameters(sessionId, sessionType);
        try {
            String paramsJsonString = params.getAsString(
                Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS
            );
            if (!StringUtils.isNullOrEmpty(paramsJsonString)) {
                JSONObject requestParameters = new JSONObject(paramsJsonString);
                return new SessionRequestParams(requestParameters);
            } else {
                DebugLogger.INSTANCE.error(TAG, "Session request parameters are empty");
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, "Failed to parse session request parameters", e);
        }
        return null;
    }

    @VisibleForTesting
    public ContentValues getSessionRequestParameters(final long sessionId, SessionType sessionType) {
        ContentValues queryParameters = new ContentValues();

        mReadLock.lock();
        Cursor dataCursor = null;
        try {
            final SQLiteDatabase rDatabase = mStorage.getReadableDatabase();
            if (rDatabase != null) {
                dataCursor = rDatabase.query(
                    SessionTable.TABLE_NAME,
                    new String[]{SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS},
                    SessionTableEntry.FIELD_SESSION_ID + " = ? AND " + SessionTableEntry.FIELD_SESSION_TYPE + " = ?",
                    new String[]{String.valueOf(sessionId), String.valueOf(sessionType.getCode())},
                    null, null, null,
                    "1"
                );
                if (dataCursor.moveToNext()) {
                    final ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(dataCursor, values);
                    queryParameters = values;
                }
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                exception,
                "Smth was wrong while getting query parameters for session = %s",
                sessionId
            );
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
            whereClause.append(arg).append(" = ? ");
        }

        return TextUtils.isEmpty(whereClause.toString()) ? null : whereClause.toString();
    }

    private static String [] formWhereArgs(final String [] preWhereArgs, final Map<String, String> extraSelection) {
        final List<String> whereArgs = new ArrayList<String>(Arrays.asList(preWhereArgs));

        for (final Map.Entry<String, String> selectionEntry : extraSelection.entrySet()) {
            whereArgs.add(selectionEntry.getValue());
        }

        return whereArgs.toArray(new String[0]);
    }

    private static int getReportTypeFromContentValues(final ContentValues values) {
        final Integer eventTypeId = values.getAsInteger(EventTableEntry.FIELD_EVENT_TYPE);
        return eventTypeId != null ? eventTypeId : -1;
    }

    private int getReportType(ContentValues reportItem) {
        return reportItem.getAsInteger(EventTableEntry.FIELD_EVENT_TYPE);
    }
}
