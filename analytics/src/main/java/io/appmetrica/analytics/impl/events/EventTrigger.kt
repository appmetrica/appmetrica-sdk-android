package io.appmetrica.analytics.impl.events

internal interface EventTrigger {

    fun trigger()

    fun triggerAsync()

    fun forceTrigger()

    fun enableTrigger()

    fun disableTrigger()
}
