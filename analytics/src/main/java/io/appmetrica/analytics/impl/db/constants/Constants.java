package io.appmetrica.analytics.impl.db.constants;

import android.provider.BaseColumns;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider;
import io.appmetrica.analytics.impl.db.DatabaseScriptsProvider;
import io.appmetrica.analytics.impl.db.DbTablesColumnsProvider;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import java.util.List;
import java.util.Locale;

public final class Constants {
    public static final Boolean PROFILE_SQL = false;

    // Database version similar to level of SDK's API
    public static final int DATABASE_VERSION = AppMetrica.getLibraryApiLevel();

    // Old databases
    public static final String OLD_COMPONENT_DATABASE_PREFIX = "db_metrica_";
    public static final String OLD_SERVICE_DATABASE = "metrica_data.db";
    public static final String OLD_CLIENT_DATABASE = "metrica_client_data.db";

    // Databases
    public static final String DATABASES_RELATIVE_PATH = FileUtils.SDK_STORAGE_RELATIVE_PATH + "/db";
    public static final String PRE_LOLLIPOP_DATABASE_PREFIX = FileUtils.SDK_FILES_PREFIX;
    public static final String COMPONENT_DB_PATTERN = "component_%s.db";
    public static final String SERVICE_MAIN_DATABASE = "service_main.db";
    public static final String CLIENT_MAIN_DATABASE = "client.db";

    // Update scripts
    private static final DatabaseScriptsProvider sDbScriptsProvider = new DatabaseScriptsProvider();
    private static final DbTablesColumnsProvider sDbTablesColumnsProvider = new DbTablesColumnsProvider();
    private static final DatabaseManagerProvider sDatabaseManagerProvider =
            new DatabaseManagerProvider(sDbScriptsProvider, sDbTablesColumnsProvider);

    @VisibleForTesting
    public static HashMultimap<Integer, DatabaseScript> getUpgradeDbScript() {
        return sDbScriptsProvider.getComponentDatabaseUpgradeDbScripts();
    }

    @VisibleForTesting
    public static HashMultimap<Integer, DatabaseScript> getUpgradeServiceDbScripts() {
        return sDbScriptsProvider.getUpgradeServiceDbScripts();
    }

    // Reports
    public static final class EventsTable {

        public static final String TABLE_NAME = "events";

        public static final List<String> ACTUAL_COLUMNS = CollectionUtils.createSortedListWithoutRepetitions(
            EventTableEntry.FIELD_EVENT_ID,
            EventTableEntry.FIELD_EVENT_SESSION,
            EventTableEntry.FIELD_EVENT_SESSION_TYPE,
            EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION,
            EventTableEntry.FIELD_EVENT_TYPE,
            EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER,
            EventTableEntry.FIELD_EVENT_TIME,
            EventTableEntry.FIELD_EVENT_DESCRIPTION
        );

        public static abstract class EventTableEntry implements BaseColumns {
            public static final String FIELD_EVENT_ID = "id";
            public static final String FIELD_EVENT_SESSION = "session_id";
            public static final String FIELD_EVENT_SESSION_TYPE = "session_type";
            public static final String FIELD_EVENT_NUMBER_IN_SESSION = "number_in_session";
            public static final String FIELD_EVENT_TYPE = "type";
            public static final String FIELD_EVENT_GLOBAL_NUMBER = "global_number";
            public static final String FIELD_EVENT_TIME = "time";
            public static final String FIELD_EVENT_DESCRIPTION = "event_description";
        }

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + EventTableEntry.FIELD_EVENT_ID + " INTEGER PRIMARY KEY,"
            + EventTableEntry.FIELD_EVENT_SESSION + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_SESSION_TYPE + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_TYPE + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_TIME + " INTEGER,"
            + EventTableEntry.FIELD_EVENT_DESCRIPTION + " BLOB"
            + " )";

        public static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String DELETE_TOP_RECORDS_WHERE =
                "%1$s = %2$s AND " +
                "%3$s = %4$s AND " +
                "%5$s <= (SELECT %5$s FROM %6$s WHERE %1$s = %2$s AND %3$s = %4$s ORDER BY %5$s ASC LIMIT %7$s, 1)";

        /*
        id IN (
          SELECT id FROM events
            ORDER BY
              CASE
                WHEN type IN (0, 6145, 4096, 4097, 8224) THEN 2 //EVENTS_WITH_FIRST_HIGHEST_PRIORITY
                WHEN type IN (12290) THEN 1 //EVENTS_WITH_SECOND_HIGHEST_PRIORITY
                ELSE 0
              END,
              id
            LIMIT (SELECT count() FROM events) / percent
         )
         */
        public static final String DELETE_EXCESSIVE_RECORDS_WHERE =
            EventTableEntry.FIELD_EVENT_ID + " IN (" +
                "SELECT " + EventTableEntry.FIELD_EVENT_ID + " " +
                "FROM " + TABLE_NAME + " " +
                "ORDER BY CASE " +
                "WHEN " + EventTableEntry.FIELD_EVENT_TYPE + " IN (%1$s) THEN 2 " +
                "WHEN " + EventTableEntry.FIELD_EVENT_TYPE + " IN (%2$s) THEN 1 " +
                "ELSE 0 END, " + EventTableEntry.FIELD_EVENT_ID + " " +
                "LIMIT (SELECT count() FROM " + TABLE_NAME + ") / %3$s)";

        public static final String SELECT_BY_SESSION_WHERE = EventTableEntry.FIELD_EVENT_SESSION + " = ? AND "
                + EventTableEntry.FIELD_EVENT_SESSION_TYPE + " = ?";

    }

    // Sessions
    public static final class SessionTable {

        public static final String TABLE_NAME = "sessions";

        public static final List<String> ACTUAL_COLUMNS = CollectionUtils.createSortedListWithoutRepetitions(
            SessionTableEntry.FIELD_SESSION_ID,
            SessionTableEntry.FIELD_SESSION_TYPE,
            SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS,
            SessionTableEntry.FIELD_SESSION_DESCRIPTION
        );

        public static abstract class SessionTableEntry implements BaseColumns {

            public static final String FIELD_SESSION_ID = "id";
            public static final String FIELD_SESSION_TYPE = "type";
            public static final String FIELD_SESSION_REPORT_REQUEST_PARAMETERS = "report_request_parameters";
            public static final String FIELD_SESSION_DESCRIPTION = "session_description";
        }

        public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + SessionTableEntry.FIELD_SESSION_ID + " INTEGER,"
                + SessionTableEntry.FIELD_SESSION_TYPE + " INTEGER,"
                + SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS + " TEXT,"
                + SessionTableEntry.FIELD_SESSION_DESCRIPTION + " BLOB"
            + " )";

        public static final String DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        /*
            Select only not empty sessions. Empty sessions will be cleared during report sending.
            Works similarly to SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS
        */

        /*
        SELECT DISTINCT report_request_parameters
        FROM sessions
        WHERE id > 0
        AND (
            SELECT count()
            FROM reports
            WHERE sessions.id = events.session_id
            AND session.type = events.session_type
        ) > 0
        ORDER BY id
        LIMIT 1
         */
        public static final String DISTINCT_REPORT_REQUEST_PARAMETERS = String.format(Locale.US,
                "SELECT DISTINCT %s " +
                        " FROM %s WHERE %s >=0 AND" +
                        " (SELECT count() FROM %5$s" +
                        " WHERE %5$s.%6$s = %2$s.%3$s AND %5$s.%7$s = %2$s.%4$s) > 0" +
                        " ORDER BY %3$s LIMIT 1",
                SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS,
                TABLE_NAME,
                SessionTableEntry.FIELD_SESSION_ID,
                SessionTableEntry.FIELD_SESSION_TYPE,
                EventsTable.TABLE_NAME,
                EventsTable.EventTableEntry.FIELD_EVENT_SESSION,
                EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE);
        public static final String QUERY_GET_SESSION_REQUEST_PARAMETERS =
                "SELECT " + SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS +
                        " FROM " + TABLE_NAME +
                        " WHERE " + SessionTableEntry.FIELD_SESSION_ID + " = %s AND " +
                        SessionTableEntry.FIELD_SESSION_TYPE + " = %s " +
                        "ORDER BY " + SessionTableEntry.FIELD_SESSION_ID + " DESC " +
                        "LIMIT 1";

        /*
        Full query:
         delete
         from sessions
         where cast(sessions.id as integer) < ?
         and
         (
              select count(events.id)
              from events
              where
              events.session_id = sessions.id
         ) = 0
         */
        public static final String CLEAR_EMPTY_PREVIOUS_SESSIONS =
                String.format(Locale.US,
                                "(select count(%s.%s) " +
                                "from %s " +
                                "where %s.%s = %s.%s) = 0 " +
                                "and cast(%s as integer) < ?",
                        EventsTable.TABLE_NAME,
                        EventsTable.EventTableEntry.FIELD_EVENT_ID,
                        EventsTable.TABLE_NAME,
                        EventsTable.TABLE_NAME,
                        EventsTable.EventTableEntry.FIELD_EVENT_SESSION,
                        SessionTable.TABLE_NAME,
                        SessionTableEntry.FIELD_SESSION_ID,
                        SessionTableEntry.FIELD_SESSION_ID
                );

        // Queries for debugging sessions logic.
        public static final String ALL_SESSION = " SELECT DISTINCT " + SessionTableEntry.FIELD_SESSION_ID + " From "
                + SessionTable.TABLE_NAME + " order by " + SessionTableEntry.FIELD_SESSION_ID + " asc ";
        public static final String ALL_SESSION_IN_REPORTS = " SELECT DISTINCT " +
            EventsTable.EventTableEntry.FIELD_EVENT_SESSION
                + " From " + EventsTable.TABLE_NAME + " order by " +
            EventsTable.EventTableEntry.FIELD_EVENT_SESSION + " asc ";

    }

    public static final class BinaryDataTable {

        public static final String TABLE_NAME = "binary_data";

        public static final String DATA_KEY = "data_key";
        public static final String VALUE = "value";

        public static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + DATA_KEY + " TEXT PRIMARY KEY,"
                        + VALUE + " BLOB"
                        + ")";

        public static final String DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final List<String> ACTUAL_COLUMNS = CollectionUtils.createSortedListWithoutRepetitions(
                BinaryDataTable.DATA_KEY,
                BinaryDataTable.VALUE
        );
    }

    public static final class PreferencesTable implements KeyValueTable {

        public static final String TABLE_NAME = "preferences";

        public static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + KeyValueTableEntry.FIELD_KEY + " TEXT PRIMARY KEY,"
                        + KeyValueTableEntry.FIELD_VALUE + " TEXT,"
                        + KeyValueTableEntry.FIELD_TYPE + " INTEGER"
                        + ")";

        public static final String DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public interface KeyValueTable {

        int BOOL = 1;
        int INT = 2;
        int LONG = 3;
        int STRING = 4;
        int FLOAT = 5;

        List<String> ACTUAL_COLUMNS = CollectionUtils.createSortedListWithoutRepetitions(
                KeyValueTableEntry.FIELD_KEY,
                KeyValueTableEntry.FIELD_VALUE,
                KeyValueTableEntry.FIELD_TYPE
        );

        interface KeyValueTableEntry extends BaseColumns {
            String FIELD_KEY = "key";
            String FIELD_VALUE = "value";
            String FIELD_TYPE = "type";
        }

        String DELETE_WHERE_KEY = KeyValueTableEntry.FIELD_KEY + " = ?";
    }

    public static final class RequestParametersJsonKeys {
        public static final String DEVICE_ID = "dId";
        public static final String UUID = "uId";
        public static final String ANALYTICS_SDK_VERSION_NAME = "analyticsSdkVersionName";
        public static final String ANALYTICS_SDK_BUILD_NUMBER = "kitBuildNumber";
        public static final String ANALYTICS_SDK_BUILD_TYPE = "kitBuildType";
        public static final String OS_VERSION = "osVer";
        public static final String OS_API_LEVEL = "osApiLev";
        public static final String APP_VERSION = "appVer";
        public static final String APP_BUILD = "appBuild";
        public static final String LOCALE = "lang";
        public static final String ROOT_STATUS = "root";
        public static final String APP_DEBUGGABLE = "app_debuggable";
        public static final String APP_FRAMEWORK = "app_framework";
        public static final String ATTRIBUTION_ID = "attribution_id";
    }

    public static DatabaseManagerProvider getDatabaseManagerProvider() {
        return sDatabaseManagerProvider;
    }

}
