package io.appmetrica.analytics.coreapi.internal.data

fun interface Updatable<T> {
    fun update(value: T)
}
