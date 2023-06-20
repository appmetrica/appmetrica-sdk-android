package io.appmetrica.analytics.impl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UpgradeDatabaseHelper extends SQLiteOpenHelper {

    private final TablesManager mManager;

    public UpgradeDatabaseHelper(Context context, TablesManager manager, int newestVersion) {
        super(context, "test_table", null, newestVersion);
        mManager = manager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mManager.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mManager.onUpgrade(db, oldVersion, newVersion);
    }
}
