package io.appmetrica.analytics.impl.db.preferences;

public class PreferencesItem {

    private final String mKey;
    private final String mFullKey;

    public PreferencesItem(final String key) {
        this(key, null);
    }

    public PreferencesItem(final String key, final String keySuffix) {
        mKey = key;
        mFullKey = key(keySuffix);
    }

    public String key() {
        return mKey;
    }

    public String fullKey() {
        return mFullKey;
    }

    public final String key(final String keySuffix) {
        return (null != keySuffix) ? (mKey + keySuffix) : mKey;
    }

}
