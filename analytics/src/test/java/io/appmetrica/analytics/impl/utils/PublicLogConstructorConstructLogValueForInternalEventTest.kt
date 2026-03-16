package io.appmetrica.analytics.impl.utils

import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class PublicLogConstructorConstructLogValueForInternalEventTest : CommonTest() {

    private val message = "Some message"
    private val name = "event name"
    private val value = "event value"

    @Test
    fun `returns null for null type`() {
        assertThat(
            PublicLogConstructor.constructLogValueForInternalEvent(message, null, name, value)
        ).isNull()
    }

    @Test
    fun `returns null for non-public event type`() {
        assertThat(
            PublicLogConstructor.constructLogValueForInternalEvent(
                message, InternalEvents.EVENT_TYPE_UNDEFINED, name, value
            )
        ).isNull()
    }

    @Test
    fun `returns message with event type for public event without name and value`() {
        assertThat(
            PublicLogConstructor.constructLogValueForInternalEvent(
                message, InternalEvents.EVENT_TYPE_INIT, null, null
            )
        ).isEqualTo("$message: ${InternalEvents.EVENT_TYPE_INIT.name}")
    }

    @Test
    fun `includes name and value in result when both are present`() {
        assertThat(
            PublicLogConstructor.constructLogValueForInternalEvent(
                message, InternalEvents.EVENT_TYPE_REGULAR, name, value
            )
        ).isEqualTo("$message: ${InternalEvents.EVENT_TYPE_REGULAR.name} with name $name with value $value")
    }

    @Test
    fun `omits name from result when name is null or empty`() {
        for (emptyName in listOf(null, "")) {
            assertThat(
                PublicLogConstructor.constructLogValueForInternalEvent(
                    message, InternalEvents.EVENT_TYPE_REGULAR, emptyName, value
                )
            ).doesNotContain("with name")
        }
    }

    @Test
    fun `omits value from result when value is null or empty`() {
        for (emptyValue in listOf(null, "")) {
            assertThat(
                PublicLogConstructor.constructLogValueForInternalEvent(
                    message, InternalEvents.EVENT_TYPE_REGULAR, name, emptyValue
                )
            ).doesNotContain("with value")
        }
    }
}
