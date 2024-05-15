package io.appmetrica.analytics.impl.db.connectors;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class SimpleDBConnector implements DBConnector {

    private static final String TAG = "[SimpleDBConnector]";

    private final DatabaseStorage mStorage;

    public SimpleDBConnector(DatabaseStorage storage) {
        mStorage = storage;
    }

    @Nullable
    public SQLiteDatabase openDb() {
        try {
            return mStorage.getWritableDatabase();
        } catch (Throwable ex) {
            DebugLogger.warning(TAG,"Something went wrong while opening database\n" + ex);
        }
        return null;
    }

    public void closeDb(@Nullable final SQLiteDatabase db) {}
}
