package io.appmetrica.analytics.remotepermissions.internal

import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.remotepermissions.impl.FeatureConfigToProtoBytesConverter
import io.appmetrica.analytics.remotepermissions.impl.FeatureParser
import io.appmetrica.analytics.remotepermissions.impl.RemoteConfigPermissionStrategy
import io.appmetrica.analytics.remotepermissions.internal.config.FeatureConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
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
    private val featureConfig = mock<FeatureConfig> {
        on { permittedPermissions } doReturn permittedPermissions
    }
    private val moduleRemoteConfig = mock<ModuleRemoteConfig<FeatureConfig?>> {
        on { featuresConfig } doReturn featureConfig
    }

    private val firstUpdatedPermittedPermissions = setOf("third")
    private val firstFeatureUpdatedConfig = mock<FeatureConfig> {
        on { permittedPermissions } doReturn firstUpdatedPermittedPermissions
    }
    private val firstUpdatedModuleConfig = mock<ModuleRemoteConfig<FeatureConfig?>> {
        on { featuresConfig } doReturn firstFeatureUpdatedConfig
    }

    private val secondUpdatedPermittedPermissions = setOf("first", "second", "third")
    private val secondFeatureUpdatedConfig = mock<FeatureConfig> {
        on { permittedPermissions } doReturn secondUpdatedPermittedPermissions
    }
    private val secondUpdatedModuleConfig = mock<ModuleRemoteConfig<FeatureConfig?>> {
        on { featuresConfig } doReturn secondFeatureUpdatedConfig
    }

    @get:Rule
    val remoteConfigPermissionStrategyMockedConstructionRule =
        MockedConstructionRule(RemoteConfigPermissionStrategy::class.java)

    @get:Rule
    val featuresParserMockedConstructionRule = MockedConstructionRule(FeatureParser::class.java)

    @get:Rule
    val featureConfigToProtoBytesConverterMockedConstructionRule =
        MockedConstructionRule(FeatureConfigToProtoBytesConverter::class.java)

    private lateinit var remotePermissionsModuleEntryPoint: RemotePermissionsModuleEntryPoint

    private lateinit var parser: FeatureParser
    private lateinit var converter: FeatureConfigToProtoBytesConverter

    @Before
    fun setUp() {
        remotePermissionsModuleEntryPoint = RemotePermissionsModuleEntryPoint()

        parser = featureParser()
        converter = featureConfigToProtoBytesConverter()
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
        whenever(featureConfig.permittedPermissions).thenReturn(setOf("third", "second"))
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
        whenever(featureConfig.permittedPermissions).thenReturn(emptySet())
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
        whenever(firstFeatureUpdatedConfig.permittedPermissions).thenReturn(emptySet())
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

    private fun featureParser(): FeatureParser {
        assertThat(featuresParserMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(featuresParserMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return featuresParserMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun featureConfigToProtoBytesConverter(): FeatureConfigToProtoBytesConverter {
        assertThat(featureConfigToProtoBytesConverterMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(featureConfigToProtoBytesConverterMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
        return featureConfigToProtoBytesConverterMockedConstructionRule.constructionMock.constructed().first()
    }
}
