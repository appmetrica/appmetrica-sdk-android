package io.appmetrica.analytics.coreutils.internal.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.coreutils.internal.io.CloseableUtilsKt;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

public class DBUtils {

    private static final String TAG = "[DBUtils]";

    public static void cursorRowToContentValues(Cursor cursor, ContentValues values) {
        try {
            enhancedCursorRowToContentValues(cursor, values);
        } catch (Throwable ex) {
            YLogger.e(ex, "%s Something went wrong while filling content values from cursor", TAG);
        }
    }

    public static void enhancedCursorRowToContentValues(Cursor cursor, ContentValues values) {
        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_BLOB:
                    values.put(columns[i], cursor.getBlob(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put(columns[i], cursor.getDouble(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put(columns[i], cursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    values.put(columns[i], cursor.getString(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    values.put(columns[i], cursor.getString(i));
                    break;
                default:
                    values.put(columns[i], cursor.getString(i));
                    break;
            }
        }
    }

    public static long queryRowsCount(final SQLiteDatabase db, final String tableName) {
        Cursor cursor = null;
        long rowsCount = 0;
        try {
            cursor = db.rawQuery("SELECT count() FROM " + tableName, null);
            if (cursor.moveToFirst()) {
                rowsCount = cursor.getLong(0);
            }
        } finally {
            CloseableUtilsKt.closeSafely(cursor);
        }
        return rowsCount;
    }
}
