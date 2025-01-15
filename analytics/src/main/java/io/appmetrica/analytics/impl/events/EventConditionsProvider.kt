package io.appmetrica.analytics.impl.events

interface EventConditionsProvider {

    fun getCommonEventConditions(): List<EventCondition>

    fun getForceSendEventConditions(): List<EventCondition>
}
