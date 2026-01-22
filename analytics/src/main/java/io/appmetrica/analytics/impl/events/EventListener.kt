package io.appmetrica.analytics.impl.events

internal interface EventListener {

    fun onEventsAdded(reportTypes: List<Int>)

    fun onEventsRemoved(reportTypes: List<Int>)

    fun onEventsUpdated()
}
