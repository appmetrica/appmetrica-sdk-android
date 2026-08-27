package io.appmetrica.analytics.impl.db;

import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.db.constants.Constants;

public class DatabaseRevisions {

    public interface DatabaseRevision {

        int getVersion();

        void onCreate(SQLiteDatabase db);
    }

    public static class DatabaseV112 implements DatabaseRevision {
        public int getVersion() {
            return 112;
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(getReportsCreateScript());
            db.execSQL(getSessionCreateScript());
            db.execSQL(getPreferenceCreateScript());
            db.execSQL(getBinaryDataTableCreateScript());
        }

        public String getSessionCreateScript() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.SessionTable.TABLE_NAME + " ("
                + Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID + " INTEGER,"
                + Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE + " INTEGER DEFAULT " + SessionType.FOREGROUND.getCode() + ","
                + Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS + " TEXT,"
                + Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION + " BLOB"
                + " )";
        }

        public String getPreferenceCreateScript() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.PreferencesTable.TABLE_NAME + " ("
                + Constants.PreferencesTable.KeyValueTableEntry.FIELD_KEY + " TEXT PRIMARY KEY,"
                + Constants.PreferencesTable.KeyValueTableEntry.FIELD_VALUE + " TEXT,"
                + Constants.PreferencesTable.KeyValueTableEntry.FIELD_TYPE + " INTEGER"
                + ")";
        }

        public String getReportsCreateScript() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.EventsTable.TABLE_NAME + " ("
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_ID + " INTEGER PRIMARY KEY,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME + " INTEGER,"
                + Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION + " BLOB"
                + " )";
        }

        public String getBinaryDataTableCreateScript() {
            return "CREATE TABLE IF NOT EXISTS " + Constants.BinaryDataTable.TABLE_NAME + " ("
                + Constants.BinaryDataTable.DATA_KEY + " TEXT PRIMARY KEY,"
                + Constants.BinaryDataTable.VALUE + " BLOB"
                + ")";
        }
    }

    public static class ServiceDatabaseV112 implements DatabaseRevision {

        public int getVersion() {
            return 112;
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Constants.PreferencesTable.CREATE_TABLE);
            db.execSQL(Constants.BinaryDataTable.CREATE_TABLE);
        }
    }
}
