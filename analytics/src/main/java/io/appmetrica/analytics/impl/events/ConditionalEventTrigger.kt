package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.atomic.AtomicBoolean

class ConditionalEventTrigger(
    private val eventsFlusher: EventsFlusher,
    private val conditions: List<EventCondition>,
    private val forceSendConditions: List<EventCondition>,
    componentId: ComponentId
) : EventTrigger {

    private val tag = "[ConditionalEventTrigger - $componentId]"

    private val triggerEnabled = AtomicBoolean(true)

    override fun trigger() {
        if (triggerEnabled.get()) {
            sendEventsIfNeeded()
        } else {
            DebugLogger.info(tag, "Trigger is disabled. So ignore trigger")
        }
    }

    override fun triggerAsync() {
        if (triggerEnabled.get() && allConditionsMet()) {
            eventsFlusher.flushAllTaskAsync()
        } else {
            DebugLogger.info(tag, "Trigger is disabled. So ignore trigger async")
        }
    }

    override fun forceTrigger() {
        if (triggerEnabled.get()) {
            forceSendEvents()
        } else {
            DebugLogger.info(tag, "Trigger is disabled. So ignore force trigger")
        }
    }

    override fun enableTrigger() {
        DebugLogger.info(tag, "Enable trigger and send events if need")
        triggerEnabled.set(true)
    }

    override fun disableTrigger() {
        DebugLogger.info(tag, "Disable trigger")
        triggerEnabled.set(false)
    }

    private fun sendEventsIfNeeded() {
        if (allConditionsMet()) {
            DebugLogger.info(tag, "SendEventsIfNeeded: conditions are met - send")
            sendEvents()
        } else {
            DebugLogger.info(tag, "SendEventsIfNeeded: conditions aren't met - ignore")
        }
    }

    private fun allConditionsMet(): Boolean = forceSendConditions.allAreMet() && conditions.anyAreMet()

    private fun forceSendEvents() {
        if (forceSendConditions.allAreMet()) {
            DebugLogger.info(tag, "ForceSendEvents: conditions are met - send")
            sendEvents()
        } else {
            DebugLogger.info(tag, "ForceSendEvents: conditions aren't met - ignore")
        }
    }

    private fun sendEvents() {
        eventsFlusher.flushAllTasks()
    }

    private fun List<EventCondition>.allAreMet() = isEmpty() || all { it.isConditionMet }

    private fun List<EventCondition>.anyAreMet() = isNotEmpty() && any { it.isConditionMet }
}
