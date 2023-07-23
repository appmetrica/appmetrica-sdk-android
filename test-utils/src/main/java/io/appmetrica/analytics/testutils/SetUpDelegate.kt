package io.appmetrica.analytics.testutils

import kotlin.reflect.KProperty

class SetUpDelegate<T : Any>(private val initialize: () -> T) {
    private lateinit var value: T

    fun setUp() {
        value = initialize()
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
}
