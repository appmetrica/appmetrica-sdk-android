package io.appmetrica.analytics.impl.db.connectors;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

public interface DBConnector {

    @Nullable
    SQLiteDatabase openDb();

    void closeDb(@Nullable SQLiteDatabase db);
}
