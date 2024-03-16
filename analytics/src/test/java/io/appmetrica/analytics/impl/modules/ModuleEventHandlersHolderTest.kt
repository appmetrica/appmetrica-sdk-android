package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleServiceEventHandler
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.UUID

class ModuleEventHandlersHolderTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val firstIdentifier = "Identifier #1"
    private val firstHandler = mock<ModuleServiceEventHandler>()

    private val firstHandlerFactory = mock<ModuleEventServiceHandlerFactory> {
        on { createEventHandler(apiKey) } doReturn firstHandler
    }

    private val secondIdentifier = "Identifier #2"
    private val secondHandler = mock<ModuleServiceEventHandler>()

    private val secondHandlerFactory = mock<ModuleEventServiceHandlerFactory> {
        on { createEventHandler(apiKey) } doReturn secondHandler
    }

    private lateinit var holder: ModuleEventHandlersHolder

    @Before
    fun setUp() {
        holder = ModuleEventHandlersHolder()
    }

    @Test
    fun `handlers if empty`() {
        assertThat(holder.getHandlers(apiKey)).isEmpty()
    }

    @Test
    fun `handlers after register one handler`() {
        holder.register(firstIdentifier, firstHandlerFactory)
        assertThat(holder.getHandlers(apiKey)).containsExactlyEntriesOf(mapOf(firstIdentifier to firstHandler))
    }

    @Test
    fun `handlers after register some handlers`() {
        holder.register(firstIdentifier, firstHandlerFactory)
        holder.register(secondIdentifier, secondHandlerFactory)
        assertThat(holder.getHandlers(apiKey)).containsExactlyEntriesOf(mapOf(
            firstIdentifier to firstHandler,
            secondIdentifier to secondHandler
        ))
    }
}
