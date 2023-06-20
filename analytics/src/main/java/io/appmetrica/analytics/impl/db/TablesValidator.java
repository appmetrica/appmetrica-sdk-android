package io.appmetrica.analytics.impl.db;

import android.database.sqlite.SQLiteDatabase;

public interface TablesValidator {

    boolean isDbSchemeValid(SQLiteDatabase database);

}
