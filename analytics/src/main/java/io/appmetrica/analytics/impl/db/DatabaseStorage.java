package io.appmetrica.analytics.impl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import java.io.Closeable;

public class DatabaseStorage extends SQLiteOpenHelper implements Closeable {

    @NonNull
    private final String mDbName;
    private final PublicLogger mPublicLogger;
    protected final TablesManager mManager;

    public DatabaseStorage(final Context context, @NonNull String dbName, final TablesManager tablesManager) {
        this(context, dbName, tablesManager, LoggerStorage.getAnonymousPublicLogger());
    }

    @VisibleForTesting
    DatabaseStorage(final Context context,
                    @NonNull String dbName,
                    final TablesManager tablesManager,
                    @NonNull PublicLogger publicLogger) {
        super(context, dbName, null, Constants.DATABASE_VERSION);
        mManager = tablesManager;
        mDbName = dbName;
        mPublicLogger = publicLogger;
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        mManager.onCreate(database);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
        mManager.onUpgrade(database, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mManager.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mManager.onOpen(db);
    }

    @Override
    @Nullable
    public SQLiteDatabase getReadableDatabase() {
        try {
            return super.getReadableDatabase();
        } catch (Throwable ex) {
            mPublicLogger.error(ex,"Could not get readable database %s due to an exception. " +
                    "AppMetrica SDK may behave unexpectedly.", mDbName);
            AppMetricaSelfReportFacade.getReporter().reportError("db_read_error", ex);
            return null;
        }
    }

    @Override
    @Nullable
    public SQLiteDatabase getWritableDatabase() {
        try {
            return super.getWritableDatabase();
        } catch (Throwable ex) {
            mPublicLogger.error(ex, "Could not get writable database %s due to an exception. " +
                    "AppMetrica SDK may behave unexpectedly.", mDbName);
            AppMetricaSelfReportFacade.getReporter().reportError("db_write_error", ex);
            return null;
        }
    }
}
