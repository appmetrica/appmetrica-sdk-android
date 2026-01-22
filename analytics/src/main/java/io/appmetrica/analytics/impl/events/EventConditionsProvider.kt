package io.appmetrica.analytics.impl.events

internal interface EventConditionsProvider {

    fun getCommonEventConditions(): List<EventCondition>

    fun getForceSendEventConditions(): List<EventCondition>
}
