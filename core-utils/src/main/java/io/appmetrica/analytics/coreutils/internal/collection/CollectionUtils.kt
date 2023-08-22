package io.appmetrica.analytics.coreutils.internal.collection

import android.os.Bundle
import java.util.AbstractMap
import java.util.Collections
import java.util.Locale

object CollectionUtils {

    @JvmStatic
    fun areCollectionsEqual(left: Collection<Any>?, right: Collection<Any>?): Boolean {
        if (left == null && right == null) {
            return true
        } else if (left == null || right == null) {
            return false
        } else if (left.size == right.size) {
            val set: HashSet<Any>
            val other: Collection<Any>

            if (left is HashSet) {
                set = left
                other = right
            } else if (right is HashSet) {
                set = right
                other = left
            } else {
                set = HashSet(left)
                other = right
            }

            for (any in other) {
                if (set.contains(any) == false) {
                    return false
                }
            }

            return true
        }

        return false
    }

    @JvmStatic
    fun <K, V> putOpt(map: MutableMap<K, V>, key: K?, value: V?) {
        if (key != null && value != null) {
            map[key] = value
        }
    }

    @JvmStatic
    fun <T> getFromMapIgnoreCase(map: Map<String, T>, key: String): T? =
        map.entries.firstOrNull { it.key.isNullOrEmpty() == false && it.key.equals(key, true) }?.value

    @JvmStatic
    fun <T> convertMapKeysToLowerCase(map: Map<String?, T>): Map<String?, T> {
        return map.mapKeys { it.key?.toLowerCase(Locale.getDefault()) }
    }

    @JvmStatic
    fun hashSetFromIntArray(input: IntArray): Set<Int> = input.toHashSet()

    @JvmStatic
    fun <K, V> getOrDefault(map: Map<K, V>, key: K, defValue: V): V = map[key] ?: defValue

    @JvmStatic
    fun <K, V> copyOf(input: MutableMap<K, V>?): Map<K, V>? = if (input.isNullOrEmpty()) null else HashMap(input)

    @JvmStatic
    fun <T> unmodifiableListCopy(original: Collection<T>): List<T> = Collections.unmodifiableList(ArrayList(original))

    @JvmStatic
    fun <K, V> unmodifiableMapCopy(original: Map<K, V>): Map<K, V> = Collections.unmodifiableMap(HashMap(original))

    @JvmStatic
    fun <K, V> unmodifiableSameOrderMapCopy(original: Map<K, V>): Map<K, V> =
        Collections.unmodifiableMap(LinkedHashMap(original))

    @JvmStatic
    fun <T> unmodifiableSetOf(vararg values: T): Set<T> = Collections.unmodifiableSet(values.toHashSet())

    @JvmStatic
    fun toIntList(array: IntArray): List<Int> = array.toList()

    @JvmStatic
    fun createSortedListWithoutRepetitions(vararg elements: String): List<String> =
        unmodifiableListCopy(elements.toSortedSet())

    @JvmStatic
    fun <K, V> getListFromMap(map: Map<K, V>?): List<Map.Entry<K, V>>? = map?.map { AbstractMap.SimpleEntry(it) }

    @JvmStatic
    fun <K, V> getMapFromList(list: List<Map.Entry<K, V>>?): Map<K, V> = list?.associateBy({ it.key }, { it.value })
        ?: LinkedHashMap()

    @JvmStatic
    fun <T> arrayListCopyOfNullableCollection(input: Collection<T>?): List<T>? = input?.toList()

    @JvmStatic
    fun <K, V> mapCopyOfNullableMap(input: Map<K, V>?): Map<K, V>? = input?.toMap()

    @JvmStatic
    fun <T> getFirstOrNull(input: List<T?>?): T? = input?.firstOrNull()

    @JvmStatic
    fun mapToBundle(input: Map<String, ByteArray>): Bundle = Bundle(input.size).apply {
        input.forEach { (key, value) ->
            putByteArray(key, value)
        }
    }

    @JvmStatic
    fun bundleToMap(input: Bundle?): Map<String, ByteArray> = HashMap<String, ByteArray>().apply {
        input?.let {
            it.keySet().forEach { key ->
                input.getByteArray(key)?.let { value ->
                    this[key] = value
                }
            }
        }
    }

    @JvmStatic
    fun <T> nullIfEmptyList(input: List<T>?): List<T>? = input?.takeIf { it.isNotEmpty() }

    @JvmStatic
    fun isNullOrEmpty(collection: Collection<*>?): Boolean = collection.isNullOrEmpty()
}
