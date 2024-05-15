package io.appmetrica.analytics.impl.db.preferences;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;

public abstract class PreferencesDbStorage {

    public static final String TAG = PreferencesDbStorage.class.getSimpleName();

    private final IKeyValueTableDbHelper mDbHelper;
    private final String mSuffix;

    public PreferencesDbStorage(final IKeyValueTableDbHelper dbHelper) {
        this(dbHelper, null);
    }

    public PreferencesDbStorage(final IKeyValueTableDbHelper storage, final String suffix) {
        mDbHelper = storage;
        mSuffix = suffix;
    }

    private String getSuffix() {
        return mSuffix;
    }

    protected PreferencesItem createItemWithPrefix(final String key) {
        return new PreferencesItem(key, getSuffix());
    }

    @SuppressWarnings("unchecked")
    protected <T extends PreferencesDbStorage> T writeString(final String key, final String value) {
        synchronized (this) {
            mDbHelper.put(key, value);
            return (T) this;
        }
    }

    @SuppressWarnings("unchecked")
    protected  <T extends PreferencesDbStorage> T writeLong(final String key, final long value) {
        synchronized (this) {
            mDbHelper.put(key, value);
            return (T) this;
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    protected <T extends PreferencesDbStorage> T writeInt(final String key, final int value) {
        synchronized (this) {
            mDbHelper.put(key, value);
            return (T) this;
        }
    }

    @SuppressWarnings("unchecked")
    protected  <T extends PreferencesDbStorage> T writeBoolean(final String key, final boolean value) {
        synchronized (this) {
            mDbHelper.put(key, value);
            return (T) this;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends PreferencesDbStorage> T writeFloat(final String key, final float value) {
        synchronized (this) {
            mDbHelper.put(key, value);
            return (T) this;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends PreferencesDbStorage> T writeStringArray(final String key, final String[] value) {
        String savedValue = null;
        try {
            JSONArray jsonArray = new JSONArray();
            for (String item : value) {
                jsonArray.put(item);
            }
            savedValue = jsonArray.toString();
        } catch (Throwable e) {
            DebugLogger.error(TAG, e, e.getMessage());
        }
        mDbHelper.put(key, savedValue);
        return (T) this;
    }

    protected <T extends PreferencesDbStorage> T writeStringList(final String key, final List<String> value) {
        return writeStringArray(key, value.toArray(new String[value.size()]));
    }

    @SuppressWarnings("unchecked")
    protected <T extends PreferencesDbStorage> T removeKey(final String key) {
        synchronized (this) {
            mDbHelper.remove(key);
            return (T) this;
        }
    }

    public void commit() {
        synchronized (this) {
            mDbHelper.commit();
        }
    }

    protected long readLong(final String key, final long defValue) {
        return mDbHelper.getLong(key, defValue);
    }

    protected int readInt(@NonNull String key, final int defValue) {
        return mDbHelper.getInt(key, defValue);
    }

    @Nullable
    String readString(@NonNull String key) {
        return mDbHelper.getString(key, null);
    }

    @Nullable
    protected String readString(@NonNull String key, @Nullable String defValue) {
        return mDbHelper.getString(key, defValue);
    }

    protected boolean readBoolean(final String key, final boolean defValue) {
        return mDbHelper.getBoolean(key, defValue);
    }

    protected boolean containsKey(@NonNull String key) {
        return mDbHelper.containsKey(key);
    }

    float readFloat(final String key, final float defValue) {
        return mDbHelper.getFloat(key, defValue);
    }

    @NonNull
    protected Set<String> keys() {
        return mDbHelper.keys();
    }

    String[] readStringArray(final String key, final String[] defValue) {
        String[] result = defValue;
        String savedValue = mDbHelper.getString(key, null);
        if (TextUtils.isEmpty(savedValue) == false) {
            try {
                JSONArray jsonArray = new JSONArray(savedValue);
                result = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i ++) {
                    result[i] = jsonArray.optString(i);
                }
            } catch (Throwable e) {
                DebugLogger.error(TAG, e, e.getMessage());
            }
        }
        return result;
    }

    @NonNull
    List<String> readStringList(@NonNull String key, @NonNull List<String> defValue) {
        String[] stringArray = readStringArray(key, defValue == null ?
                null :
                defValue.toArray(new String[defValue.size()]));
        return stringArray == null ? null : Arrays.asList(stringArray);
    }
}
