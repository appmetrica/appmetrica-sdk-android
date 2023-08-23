package io.appmetrica.analytics.impl.utils.collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMultimap<K, V> {

    private final HashMap<K, Collection<V>> mBackedMap = new HashMap<K, Collection<V>>();
    private final boolean shouldRemoveEmptyCollection;

    public HashMultimap() {
        this(false);
    }

    public HashMultimap(boolean shouldRemoveEmptyCollection) {
        this.shouldRemoveEmptyCollection = shouldRemoveEmptyCollection;
    }

    public int size() {
        return mBackedMap.size();
    }

    public int valuesCount() {
        int count = 0;
        for (Collection<V> value : mBackedMap.values()) {
            count += value.size();
        }
        return count;
    }

    public boolean isEmpty() {
        return mBackedMap.isEmpty();
    }

    public boolean containsKey(@Nullable K key) {
        return mBackedMap.containsKey(key);
    }

    public boolean containsValue(@Nullable V value) {
        for (Collection<V> values : values()) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Collection<V> get(@Nullable K key) {
        return mBackedMap.get(key);
    }

    /**
     * @param key
     * @param value
     * @return copy of oldCollection
     */
    @Nullable
    public Collection<V> put(@Nullable K key, @Nullable V value) {
        Collection<V> collection = mBackedMap.get(key);
        if (collection == null) {
            collection = createCollection();
        } else {
            collection = createCollection(collection);
        }
        collection.add(value);
        return mBackedMap.put(key, collection);
    }

    @Nullable
    public Collection<V> removeAll(@Nullable K key) {
        return mBackedMap.remove(key);
    }

    /**
     * @param key
     * @param value
     * @return copy of new collection, if value was removed. Or return {@code null}, if nothing was removed.
     */
    @Nullable
    public Collection<V> remove(@Nullable K key, @Nullable V value) {
        Collection<V> collection = mBackedMap.get(key);
        if (collection != null) {
            if (collection.remove(value)) {
                if (collection.isEmpty() && shouldRemoveEmptyCollection) {
                    mBackedMap.remove(key);
                }
                return createCollection(collection);
            }
        }
        return null;
    }

    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @NonNull
    private Collection<V> createCollection() {
        return new ArrayList<V>();
    }

    @NonNull
    private Collection<V> createCollection(@NonNull Collection<V> original) {
        return new ArrayList<V>(original);
    }

    public void clear() {
        mBackedMap.clear();
    }

    @NonNull
    public Set<K> keySet() {
        return mBackedMap.keySet();
    }

    @NonNull
    public Collection<? extends Collection<V>> values() {
        return mBackedMap.values();
    }

    @NonNull
    public Set<? extends Map.Entry<K, ? extends Collection<V>>> entrySet() {
        return mBackedMap.entrySet();
    }

    @Override
    public String toString() {
        return mBackedMap.toString();
    }
}
