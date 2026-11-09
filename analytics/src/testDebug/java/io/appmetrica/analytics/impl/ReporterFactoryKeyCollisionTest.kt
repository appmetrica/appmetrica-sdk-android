package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.reporter.MainReporterContext
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.androidtestutils.rules.LogRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

internal class ReporterFactoryKeyCollisionTest : CommonTest() {

    @get:Rule
    val logRule = LogRule()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val componentsRule = constructionRule<MainReporterComponents>()

    @get:Rule
    val mainReporterRule = constructionRule<MainReporter>()

    @get:Rule
    val manualReporterRule = constructionRule<ManualReporter>()

    @get:Rule
    val keepAliveHandlerRule = constructionRule<KeepAliveHandler>()

    @get:Rule
    val mainReporterContextRule = constructionRule<MainReporterContext>()

    private val context: Context = mock {
        on { applicationContext } doReturn it
    }
    private val processConfiguration: ProcessConfiguration = mock()
    private val reportsHandler: ReportsHandler = mock()
    private val handler: Handler = mock()
    private val startupHelper: StartupHelper = mock()
    private val logger: PublicLogger = mock()
    private val configExtension: AppMetricaConfigExtension = mock()
    private val listener: ReporterLifecycleListener = mock()
    private val occupiedConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()
    private val freeConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()
    private val manualConfig = ReporterConfig.newConfigBuilder(occupiedConfig.apiKey).build()
    private val factory by setUp {
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(listener)
        ReporterFactory(context, processConfiguration, reportsHandler, handler, startupHelper)
    }

    @Test
    fun `occupied key rejects initial main reporter construction and free key recovers`() {
        val manualReporter = factory.getOrCreateReporter(manualConfig)
        val components = componentsRule.constructionMock.constructed().single()
        clearInvocations(components, reportsHandler, startupHelper, manualReporter, listener)

        assertThatThrownBy { factory.buildOrUpdateMainReporter(occupiedConfig, logger, configExtension) }
            .isInstanceOf(ValidationException::class.java)
            .hasMessageContaining(occupiedConfig.apiKey)

        assertSoftly { softly ->
            softly.assertThat(factory.getOrCreateReporter(manualConfig)).isSameAs(manualReporter)
            softly.assertThat(mainReporterRule.constructionMock.constructed()).isEmpty()
        }
        verifyNoInteractions(components, reportsHandler, startupHelper, manualReporter, listener)

        val mainReporter = factory.buildOrUpdateMainReporter(freeConfig, logger, configExtension)
        val freeReporterConfig = ReporterConfig.newConfigBuilder(freeConfig.apiKey).build()
        assertSoftly { softly ->
            softly.assertThat(mainReporterRule.constructionMock.constructed()).containsExactly(mainReporter)
            softly.assertThat(factory.getOrCreateReporter(freeReporterConfig)).isSameAs(mainReporter)
            softly.assertThat(factory.getOrCreateReporter(manualConfig)).isSameAs(manualReporter)
        }
        verify(components).updateConfig(freeConfig, logger)
    }

    @Test
    fun `occupied key preserves anonymous reporter and free key retry preserves ownership`() {
        val anonymousConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()
        val mainReporter = factory.buildOrUpdateAnonymousMainReporter(anonymousConfig, logger, configExtension)
        val manualReporter = factory.getOrCreateReporter(manualConfig)
        val components = componentsRule.constructionMock.constructed().single()
        clearInvocations(components, mainReporter, reportsHandler, startupHelper, manualReporter, listener)

        assertThatThrownBy { factory.buildOrUpdateMainReporter(occupiedConfig, logger, configExtension) }
            .isInstanceOf(ValidationException::class.java)
            .hasMessageContaining(occupiedConfig.apiKey)

        val anonymousReporterConfig = ReporterConfig.newConfigBuilder(anonymousConfig.apiKey).build()
        assertSoftly { softly ->
            softly.assertThat(factory.getOrCreateReporter(manualConfig)).isSameAs(manualReporter)
            softly.assertThat(factory.getOrCreateReporter(anonymousReporterConfig)).isSameAs(mainReporter)
        }
        verifyNoInteractions(components, mainReporter, reportsHandler, startupHelper, manualReporter, listener)

        assertThat(factory.buildOrUpdateMainReporter(freeConfig, logger, configExtension)).isSameAs(mainReporter)
        assertThat(factory.buildOrUpdateMainReporter(freeConfig, logger, configExtension)).isSameAs(mainReporter)
        val nextConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()
        assertThat(factory.buildOrUpdateMainReporter(nextConfig, logger, configExtension)).isSameAs(mainReporter)
        val nextReporterConfig = ReporterConfig.newConfigBuilder(nextConfig.apiKey).build()
        assertSoftly { softly ->
            softly.assertThat(factory.getOrCreateReporter(nextReporterConfig)).isSameAs(mainReporter)
            softly.assertThat(factory.getOrCreateReporter(manualConfig)).isSameAs(manualReporter)
            softly.assertThat(mainReporterRule.constructionMock.constructed()).containsExactly(mainReporter)
        }
        assertThat(manualReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(manualReporterRule.constructionMock.constructed().single()).isSameAs(manualReporter)
        verify(mainReporter).updateConfig(nextConfig, configExtension)
    }
}
