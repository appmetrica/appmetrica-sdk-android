package io.appmetrica.analytics.impl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.appmetrica.analytics.impl.db.DatabaseRevisions.DatabaseRevision;

public class OldRevisionDatabaseHelper extends SQLiteOpenHelper {

    private final DatabaseRevision mStartRevision;

    public OldRevisionDatabaseHelper(Context context, DatabaseRevision startRevision) {
        super(context, "test_table", null, startRevision.getVersion());
        mStartRevision = startRevision;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mStartRevision.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
