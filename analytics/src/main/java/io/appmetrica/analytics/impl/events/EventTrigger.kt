package io.appmetrica.analytics.impl.events

interface EventTrigger {

    fun trigger()

    fun triggerAsync()

    fun forceTrigger()

    fun enableTrigger()

    fun disableTrigger()
}
