package io.appmetrica.analytics.impl.events

interface EventListener {

    fun onEventsAdded(reportTypes: List<Int>)

    fun onEventsRemoved(reportTypes: List<Int>)

    fun onEventsUpdated()
}
