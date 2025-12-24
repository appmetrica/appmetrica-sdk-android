package io.appmetrica.analytics.idsync.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.idsync.impl.IdSyncConstants
import io.appmetrica.analytics.idsync.impl.IdSyncController
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigParser
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoBytesConverter
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoConverter
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
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

    private val serviceContext: ServiceContext = mock()
    private val idSyncConfig: IdSyncConfig = mock()
    private val sdkIdentifiers: SdkIdentifiers = mock()

    private val moduleConfig: ModuleRemoteConfig<IdSyncConfig?> = mock {
        on { featuresConfig } doReturn idSyncConfig
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
            .isEqualTo(configParserRule.constructionMock.constructed().first())

        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        assertThat(configParserRule.argumentInterceptor.flatArguments())
            .containsExactly(configProtoConverterRule.constructionMock.constructed().first())

        assertThat(configProtoConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(configProtoConverterRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun `remoteExtensionConfiguration protofub converter`() {
        assertThat(moduleEntryPoint.remoteConfigExtensionConfiguration.getProtobufConverter())
            .isEqualTo(configToBytesConverterRule.constructionMock.constructed().first())

        assertThat(configToBytesConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(configToBytesConverterRule.argumentInterceptor.flatArguments())
            .containsExactly(configProtoConverterRule.constructionMock.constructed().first())

        assertThat(configProtoConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(configProtoConverterRule.argumentInterceptor.flatArguments()).isEmpty()
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
}
