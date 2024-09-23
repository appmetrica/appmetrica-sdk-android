package io.appmetrica.analytics.impl.db.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.connectors.DBConnector;
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.KeyValueTableEntry;
import io.appmetrica.analytics.impl.db.constants.Constants.PreferencesTable;
import io.appmetrica.analytics.impl.utils.executors.NamedThreadFactory;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class KeyValueTableDbHelper implements IKeyValueTableDbHelper, Closeable {

    private static final boolean DEBUG_MODE = BuildConfig.METRICA_DEBUG;

    public static final String TAG = "[KeyValueTableDbHelper]";

    private final Map<String, Object> mValues = new HashMap<String, Object>();
    private final Map<String, Object> mModified = new HashMap<String, Object>();

    private final String mTableName;

    private final Worker mWorker;
    private volatile boolean initialized;

    private final DBConnector mDbConnector;

    public KeyValueTableDbHelper(DatabaseStorage dbStorage,
                                 String tableName) {
        this(tableName, new SimpleDBConnector(dbStorage));
    }

    protected KeyValueTableDbHelper(String tableName,
                                    final DBConnector dbConnector) {
        mDbConnector = dbConnector;
        mTableName = tableName;

        mWorker = new Worker(String.format(Locale.US, NamedThreadFactory.DB_WORKER_THREAD_PATTERN,
                NamedThreadFactory.nextThreadNum()));
        mWorker.start();
    }

    private class Worker extends InterruptionSafeThread {

        public Worker(String name) {
            super(name);
        }

        @Override
        public void run() {
            synchronized (mValues) {
                loadValues();
                initialized = true;
                mValues.notifyAll();
            }

            Map<String, Object> modifiedCopy;
            while(isRunning()) {
                synchronized (this) {
                    if (mModified.size() == 0) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    modifiedCopy = new HashMap<String, Object>(mModified);
                    mModified.clear();
                }

                if (modifiedCopy.size() > 0) {
                    applyChanges(modifiedCopy);
                    modifiedCopy.clear();
                }
            }
        }
    }

    private void loadValues() {
        Cursor dataCursor = null;
        SQLiteDatabase db = null;
        try {
            db = mDbConnector.openDb();
            if (db != null) {
                dataCursor = db.query(getTableName(),
                        new String[] {KeyValueTableEntry.FIELD_KEY, KeyValueTableEntry.FIELD_VALUE,
                                KeyValueTableEntry.FIELD_TYPE},
                        null, null, null, null, null);
                while (dataCursor.moveToNext()) {
                    String key = dataCursor.getString(dataCursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_KEY));
                    String value = dataCursor
                            .getString(dataCursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_VALUE));
                    int type = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_TYPE));
                    if (TextUtils.isEmpty(key) == false) {
                        Object parsedValue = null;
                        switch (type) {
                            case Constants.KeyValueTable.BOOL:
                                parsedValue = "true".equals(value)
                                        ? Boolean.TRUE
                                        : ("false".equals(value) ? Boolean.FALSE : null);
                                break;
                            case Constants.KeyValueTable.INT:
                                parsedValue = ParseUtils.parseInt(value);
                                break;
                            case Constants.KeyValueTable.LONG:
                                parsedValue = ParseUtils.parseLong(value);
                                break;
                            case Constants.KeyValueTable.STRING:
                                parsedValue = value;
                                break;
                            case Constants.KeyValueTable.FLOAT:
                                parsedValue = ParseUtils.parseFloat(value);
                                break;
                        }
                        if (parsedValue != null) {
                            mValues.put(key, parsedValue);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, "Smth was wrong while loading preference values.");
        } finally {
            Utils.closeCursor(dataCursor);
            mDbConnector.closeDb(db);
        }
    }

    String getTableName() {
        return mTableName;
    }

    @Override
    public void commit() {
        synchronized (mWorker) {
            mWorker.notifyAll();
        }
    }

    private void applyChanges(final Map<String, Object> modifiedCopy) {
        ContentValues[] values = new ContentValues[modifiedCopy.size()];
        int i = 0;
        Iterator<Map.Entry<String, Object>> iterator = modifiedCopy.entrySet().iterator();
        for (; iterator.hasNext(); i++) {
            Map.Entry<String, Object> entry = iterator.next();
            ContentValues row = new ContentValues();
            final String key = entry.getKey();
            final Object value = entry.getValue();

            row.put(KeyValueTableEntry.FIELD_KEY, key);
            if (value == KeyValueTableDbHelper.this) {
                row.putNull(KeyValueTableEntry.FIELD_VALUE);
            } else if (value instanceof String) {
                row.put(KeyValueTableEntry.FIELD_VALUE, (String)value);
                row.put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.STRING);
            } else if (value instanceof Long) {
                row.put(KeyValueTableEntry.FIELD_VALUE, (Long)value);
                row.put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.LONG);
            } else if (value instanceof Integer) {
                row.put(KeyValueTableEntry.FIELD_VALUE, (Integer)value);
                row.put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.INT);
            } else if (value instanceof Boolean) {
                row.put(KeyValueTableEntry.FIELD_VALUE, String.valueOf(((Boolean)value).booleanValue()));
                row.put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.BOOL);
            } else if (value instanceof Float) {
                row.put(KeyValueTableEntry.FIELD_VALUE, (Float)value);
                row.put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.FLOAT);
            } else if (DEBUG_MODE && null != value) {
                throw new UnsupportedOperationException();
            }
            values[i] = row;
        }
        insertPreferences(values);
    }

    private void insertPreferences(final ContentValues[] values) {
        if (null == values) {
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = mDbConnector.openDb();
            if (db != null) {
                db.beginTransaction();
                for (ContentValues row : values) {
                    if (row.getAsString(KeyValueTableEntry.FIELD_VALUE) == null) {
                        String key = row.getAsString(KeyValueTableEntry.FIELD_KEY);
                        db.delete(getTableName(), PreferencesTable.DELETE_WHERE_KEY, new String[] {key});
                        DebugLogger.INSTANCE.info(TAG, "remove preferences from db: " + key);
                    } else {
                        db.insertWithOnConflict(getTableName(), null, row, SQLiteDatabase.CONFLICT_REPLACE);
                        DebugLogger.INSTANCE.info(
                            TAG,
                            "Write preferences in db: " + row.toString().replace("%", "%%")
                        );
                    }
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                "Smth was wrong while inserting preferences into database.\n%s\n%s",
                exception,
                values
            );
        } finally {
            Utils.endTransaction(db);
            mDbConnector.closeDb(db);
        }
    }

    @Override
    @Nullable public String getString(final String key, final String defValue) {
        Object value = getValue(key);
        if (value instanceof String) {
            return (String) value;
        }
        if (DEBUG_MODE && value != null && !(value instanceof String)) {
            throw new InvalidTypeException("String", key, value.getClass().getSimpleName());
        }
        return defValue;
    }

    @Override
    public int getInt(final String key, final int defValue) {
        Object value = getValue(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (DEBUG_MODE && value != null && !(value instanceof Integer)) {
            throw new InvalidTypeException("Integer", key, value.getClass().getSimpleName());
        }
        return defValue;
    }

    @Override
    public long getLong(final String key, final long defValue) {
        Object value = getValue(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (DEBUG_MODE && value != null && !(value instanceof Long)) {
            throw new InvalidTypeException("Long", key, value.getClass().getSimpleName());
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(final String key, final boolean defValue) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (DEBUG_MODE && value != null && !(value instanceof Boolean)) {
            throw new InvalidTypeException("Boolean", key, value.getClass().getSimpleName());
        }
        return defValue;
    }

    @Override
    public float getFloat(final String key, final float defValue) {
        Object value = getValue(key);
        if (value instanceof Float) {
            return (Float) value;
        }
        if (DEBUG_MODE && value != null && (value instanceof Float) == false) {
            throw new InvalidTypeException("Boolean", key, value.getClass().getSimpleName());
        }
        return defValue;
    }

    @Override
    public IKeyValueTableDbHelper remove(final String key) {
        synchronized (mValues) {
            waitForInit();
            mValues.remove(key);
        }
        synchronized (mWorker) {
            mModified.put(key, this);
            mWorker.notifyAll();
        }
        return this;
    }

    @Override
    public synchronized IKeyValueTableDbHelper put(final String key, final String value) {
        putValue(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(final String key, final long value) {
        putValue(key, value);
        return this;
    }

    @Override
    public synchronized IKeyValueTableDbHelper put(final String key, final int value) {
        putValue(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(final String key, final boolean value) {
        putValue(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(final String key, final float value) {
        putValue(key, value);
        return this;
    }

    @Override
    public boolean containsKey(@NonNull String key) {
        synchronized (mValues) {
            waitForInit();
            return mValues.containsKey(key);
        }
    }

    @Override
    @NonNull
    public Set<String> keys() {
        synchronized (mValues) {
            return new HashSet<>(mValues.keySet());
        }
    }

    @VisibleForTesting
    void putValue(final String key, final Object value) {
        synchronized (mValues) {
            waitForInit();
            mValues.put(key, value);
        }
        synchronized (mWorker) {
            mModified.put(key, value);
            mWorker.notifyAll();
        }
    }

    private Object getValue(String key) {
        synchronized (mValues) {
            waitForInit();
            return mValues.get(key);
        }
    }

    private void waitForInit() {
        if (initialized == false) {
            try {
                mValues.wait();
            } catch (InterruptedException e) {}
        }
    }

    static class InvalidTypeException extends RuntimeException {
        InvalidTypeException(final String expected, final String key, final String actualType) {
            super(String.format("%s expected, but key %s has value of type %s", expected, key, actualType));
        }
    }

    @VisibleForTesting
    KeyValueTableDbHelper(DatabaseStorage dbStorage) {
        mTableName = null;
        mWorker = new Worker(String.format(Locale.US, NamedThreadFactory.DB_WORKER_THREAD_PATTERN,
                NamedThreadFactory.nextThreadNum()));
        initialized = true;
        mDbConnector = new SimpleDBConnector(dbStorage);
    }

    @VisibleForTesting
    @Override
    public void close() {
        if (mWorker.isRunning()) {
            mWorker.stopRunning();
        }
    }

    @VisibleForTesting
    @NonNull
    DBConnector getDbConnector() {
        return mDbConnector;
    }
}
