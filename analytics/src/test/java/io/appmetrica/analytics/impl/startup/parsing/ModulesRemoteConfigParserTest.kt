package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ModuleRemoteConfigController
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ModulesRemoteConfigParserTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val startupJson = JsonHelper.OptJSONObject()

    private val firstIdentifier = "Identifier #1"
    private val secondIdentifier = "Identifier #2"

    private val firstModuleModel = mock<Any>()
    private val secondModuleModel = mock<Any>()

    private val firstModuleController = mock<ModuleRemoteConfigController> {
        on { parse(startupJson) } doReturn firstModuleModel
    }
    private val secondModuleController = mock<ModuleRemoteConfigController> {
        on { parse(startupJson) } doReturn secondModuleModel
    }

    private val controllers = mapOf(
        firstIdentifier to firstModuleController,
        secondIdentifier to secondModuleController
    )

    private val mapCaptor = argumentCaptor<Map<String, Any>>()

    private val modulesController = mock<ServiceModulesController> {
        on { collectRemoteConfigControllers() } doReturn controllers
    }

    private val startupResult = mock<StartupResult>()

    private lateinit var moduleRemoteConfigParser: ModulesRemoteConfigsParser

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().modulesController).thenReturn(modulesController)
        moduleRemoteConfigParser = ModulesRemoteConfigsParser()
    }

    @Test
    fun parse() {
        moduleRemoteConfigParser.parse(startupResult, startupJson)
        verify(startupResult).setModuleRemoteConfigs(mapCaptor.capture())
        assertThat(mapCaptor.allValues)
            .containsExactly(mapOf(firstIdentifier to firstModuleModel, secondIdentifier to secondModuleModel))
    }

    @Test
    fun parseForSingleModuleParser() {
        whenever(modulesController.collectRemoteConfigControllers())
            .doReturn(mapOf(firstIdentifier to firstModuleController))
        moduleRemoteConfigParser.parse(startupResult, startupJson)
        verify(startupResult).setModuleRemoteConfigs(mapCaptor.capture())
        assertThat(mapCaptor.allValues).containsExactly(mapOf(firstIdentifier to firstModuleModel))
    }

    @Test
    fun parseForEmptyControllers() {
        whenever(modulesController.collectRemoteConfigControllers()).doReturn(emptyMap())
        moduleRemoteConfigParser.parse(startupResult, startupJson)
        verify(startupResult).setModuleRemoteConfigs(mapCaptor.capture())
        assertThat(mapCaptor.allValues).containsExactly(emptyMap())
    }
}
