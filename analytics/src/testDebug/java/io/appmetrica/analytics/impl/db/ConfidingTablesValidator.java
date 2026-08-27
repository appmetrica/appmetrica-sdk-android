package io.appmetrica.analytics.impl.db;

import android.database.sqlite.SQLiteDatabase;

public class ConfidingTablesValidator implements TablesValidator {

    public boolean isDbSchemeValid(SQLiteDatabase database) {
        return true;
    }

}
