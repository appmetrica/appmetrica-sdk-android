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
        }
    }

    override fun forceTrigger() {
        if (triggerEnabled.get()) {
            forceSendEvents()
        }
    }

    override fun enableTrigger() {
        triggerEnabled.set(true)
    }

    override fun disableTrigger() {
        triggerEnabled.set(false)
    }

    private fun sendEventsIfNeeded() {
        if (forceSendConditions.allAreMet() && conditions.anyAreMet()) {
            DebugLogger.info(tag, "sendEventsIfNeeded: conditions are met - send")
            sendEvents()
        } else {
            DebugLogger.info(tag, "sendEventsIfNeeded: conditions aren't met - ignore")
        }
    }

    private fun forceSendEvents() {
        if (forceSendConditions.allAreMet()) {
            DebugLogger.info(tag, "forceSendEvents: conditions are met - send")
            sendEvents()
        } else {
            DebugLogger.info(tag, "forceSendEvents: conditions aren't met - ignore")
        }
    }

    private fun sendEvents() {
        eventsFlusher.flushAllTasks()
    }

    private fun List<EventCondition>.allAreMet() = isEmpty() || all { it.isConditionMet }

    private fun List<EventCondition>.anyAreMet() = isNotEmpty() && any { it.isConditionMet }
}
