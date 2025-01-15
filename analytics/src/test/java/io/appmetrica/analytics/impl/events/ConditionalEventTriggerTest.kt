package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConditionalEventTriggerTest : CommonTest() {

    private val eventsFlusher: EventsFlusher = mock()
    private val firstEnabledCondition: EventCondition = mock {
        on { isConditionMet } doReturn true
    }
    private val secondEnabledCondition: EventCondition = mock {
        on { isConditionMet } doReturn true
    }
    private val firstDisabledCondition: EventCondition = mock()
    private val secondDisabledCondition: EventCondition = mock()
    private val componentId: ComponentId = mock()

    @Test
    fun `trigger for all allowed with initial enabled state `() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for all allowed with enabled trigger`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.enableTrigger()
        trigger.trigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for all allowed with disabled trigger`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.disableTrigger()
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `forceTrigger for all allowed with initial enabled state `() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for all allowed with enabled trigger`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.enableTrigger()
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for all allowed with disabled trigger`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.disableTrigger()
        trigger.forceTrigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `trigger for empty conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            emptyList(),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `trigger for empty force conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            emptyList(),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for empty both condition lists`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            emptyList(),
            emptyList(),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `forceTrigger for empty conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            emptyList(),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for empty force conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            emptyList(),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for empty both condition lists`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            emptyList(),
            emptyList(),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for second disabled condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondDisabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for first disabled condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstDisabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `trigger for both disabled conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstDisabledCondition, secondDisabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `trigger for first disabled force condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstDisabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `trigger for second disabled force condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondDisabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `trigger for both disabled force conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstDisabledCondition, secondDisabledCondition),
            componentId
        )
        trigger.trigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `forceTrigger for second disabled condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondDisabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for first disabled condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstDisabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for both disabled conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstDisabledCondition, secondDisabledCondition),
            listOf(firstEnabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher).flushAllTasks()
    }

    @Test
    fun `forceTrigger for first disabled force condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstDisabledCondition, secondEnabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `forceTrigger for second disabled force condition`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstEnabledCondition, secondDisabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }

    @Test
    fun `forceTrigger for both disabled force conditions`() {
        val trigger = ConditionalEventTrigger(
            eventsFlusher,
            listOf(firstEnabledCondition, secondEnabledCondition),
            listOf(firstDisabledCondition, secondDisabledCondition),
            componentId
        )
        trigger.forceTrigger()
        verify(eventsFlusher, never()).flushAllTasks()
    }
}
