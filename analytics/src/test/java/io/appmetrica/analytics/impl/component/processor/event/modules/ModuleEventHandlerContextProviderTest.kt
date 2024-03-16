package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.modules.LegacyModulePreferenceAdapter
import io.appmetrica.analytics.impl.modules.ModulePreferencesAdapter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ModuleEventHandlerContextProviderTest : CommonTest() {

    private val preferences = mock<PreferencesComponentDbStorage>()
    private val saver = mock<EventSaver>()

    private val component = mock<ComponentUnit> {
        on { componentPreferences } doReturn preferences
        on { eventSaver } doReturn saver
    }

    private val moduleIdentifier = "Module id"

    private val firstReport = mock<CounterReport>()
    private val secondReport = mock<CounterReport>()

    @get:Rule
    val legacyModulePreferencesAdapterMockedConstructionRule =
        MockedConstructionRule(LegacyModulePreferenceAdapter::class.java)

    @get:Rule
    val modulePreferencesAdapterMockedConstructionRule = MockedConstructionRule(ModulePreferencesAdapter::class.java)

    @get:Rule
    val moduleEventHandlerContextImplMockedConstructionRule =
        MockedConstructionRule(ModuleEventServiceHandlerContextImpl::class.java)

    @get:Rule
    val moduleEventReporterMockedConstructionRule = MockedConstructionRule(ModuleEventReporter::class.java)

    private lateinit var moduleEventHandlerContextProvider: ModuleEventHandlerContextProvider

    @Before
    fun setUp() {
        moduleEventHandlerContextProvider = ModuleEventHandlerContextProvider(component, moduleIdentifier)
    }

    @Test
    fun getContext() {
        val firstContext = moduleEventHandlerContextProvider.getContext(firstReport)
        val secondContext = moduleEventHandlerContextProvider.getContext(secondReport)

        assertThat(moduleEventHandlerContextImplMockedConstructionRule.constructionMock.constructed())
            .hasSize(2)
        assertThat(firstContext)
            .isEqualTo(moduleEventHandlerContextImplMockedConstructionRule.constructionMock.constructed()[0])
        assertThat(secondContext)
            .isEqualTo(moduleEventHandlerContextImplMockedConstructionRule.constructionMock.constructed()[1])

        val moduleEventReports = moduleEventReporters()
        assertThat(moduleEventHandlerContextImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                modulePreferencesAdapter(), legacyModulePreferenceAdapter(), moduleEventReports[0],
                modulePreferencesAdapter(), legacyModulePreferenceAdapter(), moduleEventReports[1]
            )
    }

    private fun legacyModulePreferenceAdapter(): LegacyModulePreferenceAdapter {
        assertThat(legacyModulePreferencesAdapterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(legacyModulePreferencesAdapterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(preferences)
        return legacyModulePreferencesAdapterMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun modulePreferencesAdapter(): ModulePreferencesAdapter {
        assertThat(modulePreferencesAdapterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(modulePreferencesAdapterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(moduleIdentifier, preferences)
        return modulePreferencesAdapterMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun moduleEventReporters(): List<ModuleEventReporter> {
        val result = moduleEventReporterMockedConstructionRule.constructionMock.constructed()
        assertThat(result).hasSize(2)
        assertThat(moduleEventReporterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(saver, firstReport, saver, secondReport)
        return result
    }
}
