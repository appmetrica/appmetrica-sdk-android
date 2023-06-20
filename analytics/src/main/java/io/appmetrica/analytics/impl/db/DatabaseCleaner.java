package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.ProtobufUtils;
import io.appmetrica.analytics.impl.SelfDiagnosticReporter;
import io.appmetrica.analytics.impl.SelfDiagnosticReporterStorage;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseCleaner {

    public enum Reason {

        BAD_REQUEST("bad_request"), DB_OVERFLOW("db_overflow");

        private final String mStringValue;

        Reason(final String stringValue) {
            mStringValue = stringValue;
        }
    }

    static class DeletionInfo {

        @Nullable
        public final List<ContentValues> selectedEvents;
        public final int mDeletedRowsCount;

        DeletionInfo(@Nullable List<ContentValues> selectedReports, int deletedRowsCount) {
            selectedEvents = selectedReports;
            mDeletedRowsCount = deletedRowsCount;
        }
    }

    private static final String TAG = "[DatabaseCleaner]";

    @NonNull
    private final CounterConfigurationReporterType mReporterType;
    @Nullable
    private final SelfDiagnosticReporterStorage mSelfDiagnosticReporterStorage;

    public DatabaseCleaner(@NonNull CounterConfigurationReporterType reporterType) {
        this(reporterType, GlobalServiceLocator.getInstance().getSelfDiagnosticReporterStorage());
    }

    @VisibleForTesting
    DatabaseCleaner(@NonNull CounterConfigurationReporterType reporterType,
                    @Nullable SelfDiagnosticReporterStorage selfDiagnosticReporterStorage) {
        mReporterType = reporterType;
        mSelfDiagnosticReporterStorage = selfDiagnosticReporterStorage;
    }

    @Nullable
    private List<ContentValues> cursorToList(@Nullable Cursor cursor) {
        List<ContentValues> values = null;
        if (cursor != null && cursor.getCount() > 0) {
            values = new ArrayList<ContentValues>(cursor.getCount());
            while (cursor.moveToNext()) {
                ContentValues contentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                values.add(contentValues);
            }
        }
        return values;
    }

    @NonNull
    public DeletionInfo cleanEvents(@NonNull SQLiteDatabase database,
                                    @NonNull String tableName,
                                    @NonNull String whereClause,
                                    @NonNull Reason reason,
                                    @Nullable String apiKey,
                                    final boolean shouldFormCleanupEvent) {
        final List<ContentValues> reports = queryReportsToDelete(database, tableName, whereClause);
        int deletedRowsCount = 0;
        if (Utils.isNullOrEmpty(reports) == false) {
            try {
                deletedRowsCount = database.delete(tableName, whereClause, null);
            } catch (Throwable ex) {
                YLogger.e(ex, "%s Could not delete rows for db", TAG);
            }
            if (shouldFormCleanupEvent) {
                YLogger.d("%sShould form EVENT_CLEANUP", TAG);
                reportCleanupEvent(reports, reason, apiKey, deletedRowsCount);
            }
            if (deletedRowsCount != reports.size()) {
                YLogger.w("%s Something wrong happened while cleaning db. " +
                        "Deleted rows: %d, rows that formed up EVENT_CLEANUP: %d",
                        TAG, deletedRowsCount, reports.size());
            } else {
                YLogger.d("%s cleared %d", TAG, deletedRowsCount);
            }
        } else {
            final Map<String, Object> eventArguments = new HashMap<String, Object>();
            eventArguments.put("table_name", tableName);
            eventArguments.put("api_key", String.valueOf(apiKey));
            AppMetricaSelfReportFacade.getReporter()
                    .reportEvent("select_rows_to_delete_failed", eventArguments);
            YLogger.w("%s Could not select rows from %s to delete.", TAG, tableName);
        }
        return new DeletionInfo(reports, deletedRowsCount);
    }

    @Nullable
    private List<ContentValues> queryReportsToDelete(@NonNull SQLiteDatabase database,
                                                     @NonNull String tableName,
                                                     @NonNull String whereClause) {
        List<ContentValues> reports = null;
        Cursor dataCursor = null;
        try {
            dataCursor = database.rawQuery(
                    String.format("SELECT %s, %s FROM %s WHERE %s",
                            Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER,
                            Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE,
                            Constants.EventsTable.TABLE_NAME,
                            whereClause
                    ),
                    null
            );
            reports = cursorToList(dataCursor);
        } catch (Throwable ex) {
            AppMetricaSelfReportFacade.getReporter().reportError("select_rows_to_delete_exception", ex);
            YLogger.e(ex, "%s Exception while selecting rows from %s to delete.", TAG, tableName);
        } finally {
            Utils.closeCursor(dataCursor);
        }
        return reports;
    }

    @Nullable
    private CounterReport formCleanupEvent(@NonNull List<ContentValues> reports,
                                           @NonNull Reason reason,
                                           @Nullable String apiKey,
                                           int actualDeletedNumber) {
        try {
            JSONObject cleared = new JSONObject();
            JSONArray globalNumbers = new JSONArray();
            JSONArray eventTypes = new JSONArray();
            for (ContentValues report : reports) {
                final Integer globalNumber = report
                        .getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER);
                final Integer internalReportType = report
                        .getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE);
                if (globalNumber != null && internalReportType != null) {
                    globalNumbers.put(globalNumber);
                    eventTypes.put(ProtobufUtils.internalEventToProto(
                            InternalEvents.valueOf(internalReportType)
                    ));
                } else {
                    YLogger.w("%s Some field was not filled, ContentValues: %s, expected to have %s and %s",
                            TAG,
                            report,
                            Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER,
                            Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE
                    );
                }
            }
            cleared.put("global_number", globalNumbers)
                    .put("event_type", eventTypes);
            JSONObject details = new JSONObject()
                    .put("reason", reason.mStringValue)
                    .put("cleared", cleared)
                    .put("actual_deleted_number", actualDeletedNumber);
            JSONObject value = new JSONObject().put("details", details);
            PublicLogger logger =
                    apiKey == null ?
                    LoggerStorage.getAnonymousPublicLogger() :
                    LoggerStorage.getOrCreatePublicLogger(apiKey);
            return EventsManager.cleanupEventReportEntry(value.toString(), logger);
        } catch (Throwable ex) {
            YLogger.e(ex, "%s Something went wrong while forming cleanup event", TAG);
        }
        return null;
    }

    private void reportCleanupEvent(@NonNull List<ContentValues> reports,
                                    @NonNull Reason reason,
                                    @Nullable String apiKey,
                                    int deletedRowsCount) {
        if (apiKey != null && mSelfDiagnosticReporterStorage != null) {
            SelfDiagnosticReporter reporter = mSelfDiagnosticReporterStorage
                    .getOrCreateReporter(apiKey, mReporterType);
            final CounterReport report = formCleanupEvent(reports, reason, apiKey, deletedRowsCount);
            if (report != null) {
                reporter.reportEvent(report);
            }
        } else {
            YLogger.w("%sEVENT_CLEANUP will not be reported.  ApiKey: %s, reporter storage: %s",
                    TAG, apiKey, mSelfDiagnosticReporterStorage);
        }
    }
}
