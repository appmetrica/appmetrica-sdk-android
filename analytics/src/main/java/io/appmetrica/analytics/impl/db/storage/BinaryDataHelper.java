package io.appmetrica.analytics.impl.db.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.connectors.DBConnector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class BinaryDataHelper implements IBinaryDataHelper {

    private static final String TAG = "[BinaryDataHelper]";

    @NonNull
    private final DBConnector mConnector;
    @NonNull
    private final String mTableName;

    public BinaryDataHelper(@NonNull DBConnector dbConnector,
                            @NonNull String tableName) {
        mConnector = dbConnector;
        mTableName = tableName;
    }

    @Override
    public void insert(@NonNull String key, @NonNull byte[] value) {
        SQLiteDatabase database = null;
        try {
            database = mConnector.openDb();
            if (database != null) {
                ContentValues values = new ContentValues();
                values.put(Constants.BinaryDataTable.DATA_KEY, key);
                values.put(Constants.BinaryDataTable.VALUE, value);
                database.insertWithOnConflict(mTableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, "could not insert %s into %s", key, mTableName);
        } finally {
            mConnector.closeDb(database);
        }
    }

    @Override
    public byte[] get(@NonNull String key) {
        SQLiteDatabase database = null;
        Cursor data = null;
        try {
            database = mConnector.openDb();
            if (database != null) {
                data = database.query(mTableName,
                        null,
                        Constants.BinaryDataTable.DATA_KEY + " = ?",
                        new String[] {key}, null, null, null);
                if (data != null && data.getCount() == 1 && data.moveToFirst()) {
                    return data.getBlob(data.getColumnIndexOrThrow(Constants.BinaryDataTable.VALUE));
                } else {
                    if (Utils.isNullOrEmpty(data) == false) {
                        DebugLogger.INSTANCE.error(TAG, "invalid cursor for key %s from %s", key, mTableName);
                    } else {
                        DebugLogger.INSTANCE.info(
                            TAG,
                            "database for key %s from %s is empty.",
                            key,
                            mTableName
                        );
                    }
                }
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, "could not get %s from %s", key, mTableName);
        } finally {
            Utils.closeCursor(data);
            mConnector.closeDb(database);
        }
        return null;
    }

    @Override
    public void remove(@NonNull String key) {
        SQLiteDatabase database = null;
        try {
            database = mConnector.openDb();
            if (database != null) {
                ContentValues values = new ContentValues();
                values.put(Constants.BinaryDataTable.DATA_KEY, key);
                database.delete(mTableName,
                        Constants.BinaryDataTable.DATA_KEY + " = ?",
                        new String[] { key });
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, "could not delete %s from %s", key, mTableName);
        } finally {
            mConnector.closeDb(database);
        }
    }

    @VisibleForTesting
    @NonNull
    DBConnector getConnector() {
        return mConnector;
    }
}
