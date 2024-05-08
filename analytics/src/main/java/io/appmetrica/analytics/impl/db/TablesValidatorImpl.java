package io.appmetrica.analytics.impl.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TablesValidatorImpl implements TablesValidator {

    private static final String TAG = "[DbTablesValidator]";

    private final String mDatabaseLogIdentifier;
    private final HashMap<String, List<String>> mTableColumnsToCheck;

    public TablesValidatorImpl(@NonNull String databaseLogIdentifier,
                               @NonNull HashMap<String, List<String>> tableColumnsToCheck) {
        mDatabaseLogIdentifier = databaseLogIdentifier;
        mTableColumnsToCheck = tableColumnsToCheck;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public HashMap<String, List<String>> getTableColumnsToCheck() {
        return mTableColumnsToCheck;
    }

    public boolean isDbSchemeValid(SQLiteDatabase db) {
        try {
            YLogger.debug(
                TAG,
                "Validate %s. Actual tables to check: %s",
                mDatabaseLogIdentifier,
                tableColumnsToString(mTableColumnsToCheck)
            );
            boolean valid = true;
            for (Map.Entry<String, List<String>> entry : mTableColumnsToCheck.entrySet()) {
                Cursor cursor = null;
                try {
                    cursor = db.query(entry.getKey(), null, null, null, null, null, null);
                    if (cursor == null) {
                        return false;
                    }
                    valid &= checkCursorColumns(cursor, entry.getKey(), entry.getValue());
                } finally {
                    Utils.closeCursor(cursor);
                }
            }
            YLogger.debug(TAG, "%s Database scheme is %s.", mDatabaseLogIdentifier,  valid ? "valid" : "invalid");

            return valid;
        } catch (Throwable e) {
            YLogger.error(TAG, e, "Exception while validating %s tables", mDatabaseLogIdentifier);
        }
        return false;
    }

    private String tableColumnsToString(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\n");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            sb.append("Table=").append(entry.getKey())
                    .append(",")
                    .append("columns=").append(entry.getValue().toString())
                    .append("\n");
        }
        sb.append("}");

        return sb.toString();
    }

    @VisibleForTesting
    boolean checkCursorColumns(@NonNull Cursor cursor, @NonNull String tableName, @NonNull List<String> columns) {
        List<String> cursorColumns = Arrays.asList(cursor.getColumnNames());
        Collections.sort(cursorColumns);
        boolean result = columns.equals(cursorColumns);

        if (result == false) {
            YLogger.error(
                TAG,
                "Invalid db: %s; for table %s expected columns: \n%s;\nactual columns:\n%s",
                mDatabaseLogIdentifier,
                tableName,
                columns.toString(),
                cursorColumns.toString()
            );
        }

        return result;
    }

}
