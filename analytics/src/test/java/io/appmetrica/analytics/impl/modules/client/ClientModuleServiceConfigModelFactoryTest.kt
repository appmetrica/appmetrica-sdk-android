package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ClientModuleServiceConfigModelFactoryTest : CommonTest() {

    private val moduleConfigBundle = MockProvider.mockedBundle()
    private val moduleIdentifier = "some_identifier"
    private val bundle = MockProvider.mockedBundle().apply {
        putBundle(moduleIdentifier, moduleConfigBundle)
    }
    private val identifiers: SdkIdentifiers = mock()
    private val moduleConfig: TestModuleConfig = mock()
    private val bundleParser: BundleToServiceConfigConverter<TestModuleConfig> = mock {
        on { fromBundle(moduleConfigBundle) } doReturn moduleConfig
    }
    private val extension: ServiceConfigExtensionConfiguration<TestModuleConfig> = mock {
        on { getBundleConverter() } doReturn bundleParser
    }

    private val factory: ClientModuleServiceConfigModelFactory by setUp { ClientModuleServiceConfigModelFactory() }

    @Test
    fun createClientModuleServiceConfigModel() {
        val config = factory.createClientModuleServiceConfigModel(
            bundle,
            moduleIdentifier,
            identifiers,
            extension
        )
        ObjectPropertyAssertions(config)
            .checkField("identifiers", identifiers)
            .checkField("featuresConfig", moduleConfig)
            .checkAll()
    }

    @Test
    fun createClientModuleServiceConfigModelIfNoSuchModule() {
        val config = factory.createClientModuleServiceConfigModel(
            bundle,
            "another_module_identifier",
            identifiers,
            extension
        )

        assertThat(config).isNull()
    }
}

private class TestModuleConfig
