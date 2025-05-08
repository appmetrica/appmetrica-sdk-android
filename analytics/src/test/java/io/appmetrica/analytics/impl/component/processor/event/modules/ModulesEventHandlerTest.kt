package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerContext
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleServiceEventHandler
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ModulesEventHandlerTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()

    private val componentId = mock<ComponentId> {
        on { apiKey } doReturn apiKey
    }

    private val component = mock<ComponentUnit> {
        on { componentId } doReturn componentId
    }

    private val currentReport = mock<CounterReport>()

    private val firstIdentifier = "First identifier"
    private val firstModuleEventHandler = mock<ModuleServiceEventHandler>()

    private val secondIdentifier = "Second identifier"
    private val secondModuleEventHandler = mock<ModuleServiceEventHandler>()

    private val thirdIdentifier = "Third identifier"
    private val thirdModuleEventHandler = mock<ModuleServiceEventHandler>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val modulesEventHandlerContextProviderMockedConstructionRule =
        MockedConstructionRule(ModuleEventHandlerContextProvider::class.java) { mock, mockedContext ->
            val eventHandlerContext = when (mockedContext.arguments()[1]) {
                firstIdentifier -> firstModuleEventHandlerContext
                secondIdentifier -> secondModuleEventHandlerContext
                thirdIdentifier -> thirdModuleEventHandlerContext
                else -> null
            }
            whenever(mock.getContext(currentReport)).thenReturn(eventHandlerContext)
        }

    private lateinit var modulesEventHandlersHolder: ModuleEventHandlersHolder

    private lateinit var modulesEventHandler: ModulesEventHandler

    private val firstModuleEventHandlerContext = mock<ModuleEventServiceHandlerContext>()
    private val secondModuleEventHandlerContext = mock<ModuleEventServiceHandlerContext>()
    private val thirdModuleEventHandlerContext = mock<ModuleEventServiceHandlerContext>()

    @Before
    fun setUp() {
        modulesEventHandlersHolder = GlobalServiceLocator.getInstance().moduleEventHandlersHolder
        whenever(modulesEventHandlersHolder.getHandlers(apiKey)).thenReturn(
            linkedMapOf(
                firstIdentifier to firstModuleEventHandler,
                secondIdentifier to secondModuleEventHandler,
                thirdIdentifier to thirdModuleEventHandler
            )
        )

        modulesEventHandler = ModulesEventHandler(component)

        initEventHandlerContextProvider()
    }

    @Test
    fun process() {
        assertThat(modulesEventHandler.process(currentReport)).isFalse()
        inOrder(firstModuleEventHandler, secondModuleEventHandler, thirdModuleEventHandler) {
            verify(firstModuleEventHandler).handle(firstModuleEventHandlerContext, currentReport)
            verify(secondModuleEventHandler).handle(secondModuleEventHandlerContext, currentReport)
            verify(thirdModuleEventHandler).handle(thirdModuleEventHandlerContext, currentReport)
        }
    }

    @Test
    fun `process if first handler break processing`() {
        whenever(firstModuleEventHandler.handle(firstModuleEventHandlerContext, currentReport)).thenReturn(true)
        assertThat(modulesEventHandler.process(currentReport)).isTrue()
        verify(firstModuleEventHandler).handle(firstModuleEventHandlerContext, currentReport)
        verifyNoMoreInteractions(secondModuleEventHandler, thirdModuleEventHandler)
    }

    @Test
    fun `process if second handler break processing`() {
        whenever(secondModuleEventHandler.handle(secondModuleEventHandlerContext, currentReport)).thenReturn(true)
        assertThat(modulesEventHandler.process(currentReport)).isTrue()
        inOrder(firstModuleEventHandler, secondModuleEventHandler) {
            verify(firstModuleEventHandler).handle(firstModuleEventHandlerContext, currentReport)
            verify(secondModuleEventHandler).handle(secondModuleEventHandlerContext, currentReport)
        }

        verifyNoMoreInteractions(thirdModuleEventHandler)
    }

    @Test
    fun `process if no module handlers`() {
        whenever(modulesEventHandlersHolder.getHandlers(apiKey)).thenReturn(LinkedHashMap())
        modulesEventHandler = ModulesEventHandler(component)
        assertThat(modulesEventHandler.process(currentReport)).isFalse()
        verifyNoMoreInteractions(firstModuleEventHandler, secondModuleEventHandler, thirdModuleEventHandler)
    }

    private fun initEventHandlerContextProvider() {
        val constructed = modulesEventHandlerContextProviderMockedConstructionRule.constructionMock.constructed()
        assertThat(constructed).hasSize(3)
        assertThat(modulesEventHandlerContextProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                component, firstIdentifier,
                component, secondIdentifier,
                component, thirdIdentifier
            )
    }
}
