package io.appmetrica.analytics.idsync.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.idsync.impl.IdSyncConstants
import io.appmetrica.analytics.idsync.impl.IdSyncController
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfig
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigParser
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoBytesConverter
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoConverter
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigWrapperJsonParser
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigWrapperProtobufConverter
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class IdSyncModuleEntryPointTest : CommonTest() {

    @get:Rule
    val configProtoConverterRule = constructionRule<IdSyncConfigToProtoConverter>()

    @get:Rule
    val configToBytesConverterRule = constructionRule<IdSyncConfigToProtoBytesConverter>()

    @get:Rule
    val configParserRule = constructionRule<IdSyncConfigParser>()

    @get:Rule
    val wrapperJsonParserRule = constructionRule<IdSyncConfigWrapperJsonParser>()

    @get:Rule
    val wrapperProtobufConverterRule = constructionRule<IdSyncConfigWrapperProtobufConverter>()

    private val serviceContext: ServiceContext = mock()
    private val idSyncConfig: IdSyncConfig = mock()
    private val idSyncConfigWrapper: IdSyncConfigWrapper = mock {
        on { config } doReturn idSyncConfig
    }
    private val sdkIdentifiers: SdkIdentifiers = mock()

    private val moduleConfig: ModuleRemoteConfig<IdSyncConfigWrapper?> = mock {
        on { featuresConfig } doReturn idSyncConfigWrapper
        on { identifiers } doReturn sdkIdentifiers
    }

    @get:Rule
    val idSyncControllerRule = constructionRule<IdSyncController>()
    private val idSyncController by idSyncControllerRule

    private val moduleEntryPoint by setUp { IdSyncModuleEntryPoint() }

    @Test
    fun identifier() {
        assertThat(moduleEntryPoint.identifier).isEqualTo(IdSyncConstants.IDENTIFIER)
    }

    @Test
    fun `remoteExtensionConfiguration features`() {
        assertThat(moduleEntryPoint.remoteConfigExtensionConfiguration.getFeatures())
            .containsExactly("is")
    }

    @Test
    fun `remoteExtensionConfiguration blocks`() {
        assertThat(moduleEntryPoint.remoteConfigExtensionConfiguration.getBlocks())
            .containsExactlyEntriesOf(mapOf("is" to 1))
    }

    @Test
    fun `remoteExtensionConfiguration json parser`() {
        assertThat(moduleEntryPoint.remoteConfigExtensionConfiguration.getJsonParser())
            .isSameAs(wrapperJsonParser())
        assertThat(wrapperJsonParserRule.argumentInterceptor.flatArguments())
            .containsExactly(configParser())
        assertThat(configParserRule.argumentInterceptor.flatArguments())
            .containsExactly(configProtoConverter())
    }

    @Test
    fun `remoteExtensionConfiguration protobuf converter`() {
        assertThat(moduleEntryPoint.remoteConfigExtensionConfiguration.getProtobufConverter())
            .isSameAs(wrapperProtobufConverter())
        assertThat(wrapperProtobufConverterRule.argumentInterceptor.flatArguments())
            .containsExactly(configToBytesConverter())
        assertThat(configToBytesConverterRule.argumentInterceptor.flatArguments())
            .containsExactly(configProtoConverter())
    }

    @Test
    fun `initServiceSide with filled config`() {
        moduleEntryPoint.initServiceSide(serviceContext, moduleConfig)
        verify(idSyncController).refresh(idSyncConfig, sdkIdentifiers)
        assertThat(idSyncControllerRule.constructionMock.constructed()).hasSize(1)
        assertThat(idSyncControllerRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext, sdkIdentifiers)
    }

    @Test
    fun `initServiceSide multiple times`() {
        repeat(10) { moduleEntryPoint.initServiceSide(serviceContext, moduleConfig) }
        verify(idSyncController).refresh(idSyncConfig, sdkIdentifiers)
        assertThat(idSyncControllerRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun `initServiceSide with empty config`() {
        moduleEntryPoint.initServiceSide(serviceContext, mock())
        verifyNoInteractions(idSyncController)
    }

    @Test
    fun `remoteExtensionConfiguration update listener before init`() {
        moduleEntryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(moduleConfig)
        assertThat(idSyncControllerRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `remoteExtensionConfiguration update listener after init`() {
        moduleEntryPoint.initServiceSide(serviceContext, moduleConfig)
        clearInvocations(idSyncController)
        moduleEntryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(moduleConfig)
        verify(idSyncController).refresh(idSyncConfig, sdkIdentifiers)
    }

    @Test
    fun `remoteExtensionConfiguration update listener after init with empty config`() {
        moduleEntryPoint.initServiceSide(serviceContext, moduleConfig)
        clearInvocations(idSyncController)
        moduleEntryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(mock())
        verifyNoInteractions(idSyncController)
    }

    private fun configProtoConverter(): IdSyncConfigToProtoConverter {
        assertThat(configProtoConverterRule.constructionMock.constructed()).hasSize(1)
        return configProtoConverterRule.constructionMock.constructed().first()
    }

    private fun configParser(): IdSyncConfigParser {
        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        return configParserRule.constructionMock.constructed().first()
    }

    private fun configToBytesConverter(): IdSyncConfigToProtoBytesConverter {
        assertThat(configToBytesConverterRule.constructionMock.constructed()).hasSize(1)
        return configToBytesConverterRule.constructionMock.constructed().first()
    }

    private fun wrapperJsonParser(): IdSyncConfigWrapperJsonParser {
        assertThat(wrapperJsonParserRule.constructionMock.constructed()).hasSize(1)
        return wrapperJsonParserRule.constructionMock.constructed().first()
    }

    private fun wrapperProtobufConverter(): IdSyncConfigWrapperProtobufConverter {
        assertThat(wrapperProtobufConverterRule.constructionMock.constructed()).hasSize(1)
        return wrapperProtobufConverterRule.constructionMock.constructed().first()
    }
}
