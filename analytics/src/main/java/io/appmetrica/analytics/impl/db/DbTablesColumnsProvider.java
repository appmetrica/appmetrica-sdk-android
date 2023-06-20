package io.appmetrica.analytics.impl.db;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.constants.Constants;
import java.util.HashMap;
import java.util.List;

public class DbTablesColumnsProvider {

    @NonNull
    private final HashMap<String, List<String>> mDbTablesColumns;

    public DbTablesColumnsProvider() {
        mDbTablesColumns = new HashMap<String, List<String>>();
        mDbTablesColumns.put(Constants.EventsTable.TABLE_NAME, Constants.EventsTable.ACTUAL_COLUMNS);
        mDbTablesColumns.put(Constants.SessionTable.TABLE_NAME, Constants.SessionTable.ACTUAL_COLUMNS);
        mDbTablesColumns.put(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS);
        mDbTablesColumns.put(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS);
    }

    @NonNull
    public HashMap<String, List<String>> getDbTablesColumns() {
        return mDbTablesColumns;
    }
}
