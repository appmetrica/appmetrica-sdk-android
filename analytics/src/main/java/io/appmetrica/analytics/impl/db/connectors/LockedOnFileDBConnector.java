package io.appmetrica.analytics.impl.db.connectors;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.impl.db.TablesManager;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocker;

public class LockedOnFileDBConnector implements DBConnector {

    private final Context mContext;
    private final String mDbName;
    @NonNull
    private final TablesManager tablesManager;
    @NonNull
    private final FileLocker mDbFileLock;

    private DatabaseStorage mStorage;

    public LockedOnFileDBConnector(Context context,
                                   final String dbPath,
                                   @NonNull TablesManager tablesManager) {
        this(context, dbPath, new FileLocker(dbPath), tablesManager);
    }

    @VisibleForTesting
    public LockedOnFileDBConnector(@NonNull Context context,
                                   @NonNull String dbName,
                                   @NonNull FileLocker databaseFileLock,
                                   @NonNull TablesManager tablesManager) {
        mContext = context;
        mDbName = dbName;
        mDbFileLock = databaseFileLock;
        this.tablesManager = tablesManager;
    }

    @WorkerThread
    @Nullable
    public synchronized SQLiteDatabase openDb() {
        try {
            mDbFileLock.lock();
            mStorage = new DatabaseStorage(mContext, mDbName, tablesManager);
            YLogger.i(this + mDbName);
            return mStorage.getWritableDatabase();
        } catch (Throwable e) {
            YLogger.e(e, "Exception while opening DatabaseStorage for %s", mDbName);
        }
        return null;
    }

    @WorkerThread
    public synchronized void closeDb(@Nullable final SQLiteDatabase db) {
        Utils.closeDatabase(db);
        Utils.closeCloseable(mStorage);
        mDbFileLock.unlock();
        mStorage = null;
    }
}
