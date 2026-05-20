package io.appmetrica.analytics.remotepermissions.internal

import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.remotepermissions.impl.RemoteConfigPermissionStrategy
import io.appmetrica.analytics.remotepermissions.impl.config.service.ServiceSideRemotePermissionsConfigConverter
import io.appmetrica.analytics.remotepermissions.impl.config.service.ServiceSideRemotePermissionsConfigParser
import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class RemotePermissionsModuleEntryPointTest : CommonTest() {

    private val serviceContext = mock<ServiceContext>()

    private val permittedPermissions = setOf("first", "second")
    private val config = mock<ServiceSideRemotePermissionsConfig> {
        on { permittedPermissions } doReturn permittedPermissions
    }
    private val wrapper = mock<ServiceSideRemotePermissionsConfigWrapper> {
        on { config } doReturn config
    }
    private val moduleRemoteConfig = mock<ModuleRemoteConfig<ServiceSideRemotePermissionsConfigWrapper?>> {
        on { featuresConfig } doReturn wrapper
    }

    private val firstUpdatedPermittedPermissions = setOf("third")
    private val firstUpdatedConfig = mock<ServiceSideRemotePermissionsConfig> {
        on { permittedPermissions } doReturn firstUpdatedPermittedPermissions
    }
    private val firstUpdatedWrapper = mock<ServiceSideRemotePermissionsConfigWrapper> {
        on { config } doReturn firstUpdatedConfig
    }
    private val firstUpdatedModuleConfig = mock<ModuleRemoteConfig<ServiceSideRemotePermissionsConfigWrapper?>> {
        on { featuresConfig } doReturn firstUpdatedWrapper
    }

    private val secondUpdatedPermittedPermissions = setOf("first", "second", "third")
    private val secondUpdatedConfig = mock<ServiceSideRemotePermissionsConfig> {
        on { permittedPermissions } doReturn secondUpdatedPermittedPermissions
    }
    private val secondUpdatedWrapper = mock<ServiceSideRemotePermissionsConfigWrapper> {
        on { config } doReturn secondUpdatedConfig
    }
    private val secondUpdatedModuleConfig = mock<ModuleRemoteConfig<ServiceSideRemotePermissionsConfigWrapper?>> {
        on { featuresConfig } doReturn secondUpdatedWrapper
    }

    @get:Rule
    val remoteConfigPermissionStrategyMockedConstructionRule =
        MockedConstructionRule(RemoteConfigPermissionStrategy::class.java)

    @get:Rule
    val parserMockedConstructionRule = MockedConstructionRule(ServiceSideRemotePermissionsConfigParser::class.java)

    @get:Rule
    val converterMockedConstructionRule =
        MockedConstructionRule(ServiceSideRemotePermissionsConfigConverter::class.java)

    private lateinit var remotePermissionsModuleEntryPoint: RemotePermissionsModuleEntryPoint

    private lateinit var parser: ServiceSideRemotePermissionsConfigParser
    private lateinit var converter: ServiceSideRemotePermissionsConfigConverter

    @Before
    fun setUp() {
        remotePermissionsModuleEntryPoint = RemotePermissionsModuleEntryPoint()

        parser = getParser()
        converter = getConverter()
    }

    @Test
    fun `askForPermissionStrategy before initServiceSide`() {
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun `askForPermissionStrategy after initServiceSide with filled feature config`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun `askForPermissionStrategy after initServiceSide with filled feature config twice`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun `askForPermissionStrategy after initServiceSide twice with different feature configs`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        whenever(config.permittedPermissions).thenReturn(setOf("third", "second"))
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun `askForPermissionStrategy after initServiceSide without feature config`() {
        whenever(moduleRemoteConfig.featuresConfig).thenReturn(null)
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun `askForPermissionStrategy after initServiceSide with empty permitted permissions`() {
        whenever(config.permittedPermissions).thenReturn(emptySet())
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        assertThat(remotePermissionsModuleEntryPoint.askForPermissionStrategy)
            .isEqualTo(remoteConfigPermissionStrategy())
    }

    @Test
    fun identifier() {
        assertThat(remotePermissionsModuleEntryPoint.identifier).isEqualTo("rp")
    }

    @Test
    fun `remoteExtensionConfiguration getFeatures`() {
        assertThat(remotePermissionsModuleEntryPoint.remoteConfigExtensionConfiguration.getFeatures())
            .isEmpty()
    }

    @Test
    fun `remoteExtensionConfiguration getBlocks`() {
        assertThat(remotePermissionsModuleEntryPoint.remoteConfigExtensionConfiguration.getBlocks())
            .containsExactlyEntriesOf(mapOf("permissions" to 1))
    }

    @Test
    fun `remoteExtensionConfiguration getJsonParser`() {
        assertThat(remotePermissionsModuleEntryPoint.remoteConfigExtensionConfiguration.getJsonParser())
            .isEqualTo(parser)
    }

    @Test
    fun `remoteExtensionConfiguration getProtobufConverter`() {
        assertThat(remotePermissionsModuleEntryPoint.remoteConfigExtensionConfiguration.getProtobufConverter())
            .isEqualTo(converter)
    }

    @Test
    fun `onRemoteConfigUpdated before initServiceSide`() {
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(firstUpdatedModuleConfig)
    }

    @Test
    fun `onRemoteConfigUpdated after initServiceSide`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(firstUpdatedModuleConfig)
        verify(remoteConfigPermissionStrategy())
            .updatePermissions(firstUpdatedPermittedPermissions)
    }

    @Test
    fun `onRemoteConfigUpdated with null features config`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        clearInvocations(remoteConfigPermissionStrategy())
        whenever(firstUpdatedModuleConfig.featuresConfig).thenReturn(null)
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(firstUpdatedModuleConfig)
        verify(remoteConfigPermissionStrategy()).updatePermissions(emptySet())
    }

    @Test
    fun `onRemoteConfigUpdated with empty permitted permissions`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        whenever(firstUpdatedConfig.permittedPermissions).thenReturn(emptySet())
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(firstUpdatedModuleConfig)
        verify(remoteConfigPermissionStrategy()).updatePermissions(emptySet())
    }

    @Test
    fun `onRemoteConfigUpdated multiple times`() {
        remotePermissionsModuleEntryPoint.initServiceSide(serviceContext, moduleRemoteConfig)
        val strategy = remoteConfigPermissionStrategy()

        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(firstUpdatedModuleConfig)
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(secondUpdatedModuleConfig)
        remotePermissionsModuleEntryPoint.onRemoteConfigUpdated(moduleRemoteConfig)

        inOrder(strategy) {
            verify(strategy).updatePermissions(firstUpdatedPermittedPermissions)
            verify(strategy).updatePermissions(secondUpdatedPermittedPermissions)
            verify(strategy).updatePermissions(permittedPermissions)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun moduleEventHandler() {
        assertThat(remotePermissionsModuleEntryPoint.moduleEventServiceHandlerFactory).isNull()
    }

    @Test
    fun locationExtension() {
        assertThat(remotePermissionsModuleEntryPoint.locationServiceExtension).isNull()
    }

    @Test
    fun moduleServiceDatabase() {
        assertThat(remotePermissionsModuleEntryPoint.moduleServicesDatabase).isNull()
    }

    private fun remoteConfigPermissionStrategy(): RemoteConfigPermissionStrategy {
        assertThat(remoteConfigPermissionStrategyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(remoteConfigPermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly()
        return remoteConfigPermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun getParser(): ServiceSideRemotePermissionsConfigParser {
        assertThat(parserMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        return parserMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun getConverter(): ServiceSideRemotePermissionsConfigConverter {
        assertThat(converterMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        return converterMockedConstructionRule.constructionMock.constructed().first()
    }
}
