package io.appmetrica.analytics.impl.db.connectors;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SimpleDBConnector implements DBConnector {
    private final DatabaseStorage mStorage;

    public SimpleDBConnector(DatabaseStorage storage) {
        mStorage = storage;
    }

    @Nullable
    public SQLiteDatabase openDb() {
        try {
            return mStorage.getWritableDatabase();
        } catch (Throwable ex) {
            YLogger.w("Something went wrong while opening database\n" + ex);
        }
        return null;
    }

    public void closeDb(@Nullable final SQLiteDatabase db) {}
}
