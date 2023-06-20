package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapWithDefault<K, V> {

    @NonNull
    private final Map<K,V> mMap;
    @NonNull
    private final V mDefaultValue;

    public MapWithDefault(@NonNull final V defaultValue) {
        this(new HashMap<K, V>(), defaultValue);
    }

    @VisibleForTesting
    public MapWithDefault(@NonNull final Map<K, V> map,
                          @NonNull final V defaultValue) {
        mMap = map;
        mDefaultValue = defaultValue;
    }

    public void put(@Nullable K key, @Nullable V value) {
        mMap.put(key, value);
    }

    @NonNull
    public V get(@Nullable K key) {
        V value = mMap.get(key);
        return value == null ? mDefaultValue : value;
    }

    @NonNull
    @VisibleForTesting
    public Map<K, V> getMap() {
        return mMap;
    }

    @NonNull
    public Set<K> keySet() {
        return mMap.keySet();
    }
}
