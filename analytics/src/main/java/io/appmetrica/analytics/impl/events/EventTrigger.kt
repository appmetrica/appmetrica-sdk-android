package io.appmetrica.analytics.impl.events

interface EventTrigger {

    fun trigger()

    fun forceTrigger()

    fun enableTrigger()

    fun disableTrigger()
}
