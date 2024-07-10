package io.appmetrica.analytics.impl.utils

object ProtobufUtils {

    inline fun <K, V, reified T> Map<K, V>.toArray(block: (Map.Entry<K, V>) -> T): Array<T> {
        val result = arrayOfNulls<T>(size)
        onEachIndexed { index, entry ->
            result[index] = block(entry)
        }
        return result as Array<T>
    }
}
