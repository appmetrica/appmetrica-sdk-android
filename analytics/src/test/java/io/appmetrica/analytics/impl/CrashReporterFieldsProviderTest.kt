package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.reporter.CrashReporterContext
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.UUID

internal class CrashReporterFieldsProviderTest : CommonTest() {

    private val userProfileId = "User profile id"
    private val config = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
        .withUserProfileID(userProfileId)
        .build()

    private val processConfiguration: ProcessConfiguration = mock()
    private val reportsHandler: ReportsHandler = mock()
    private val errorEnvironment: ErrorEnvironment = mock()

    @get:Rule
    val counterConfigurationMockedConstructionRule = constructionRule<CounterConfiguration>()
    private val counterConfiguration: CounterConfiguration by counterConfigurationMockedConstructionRule

    @get:Rule
    val reporterEnvironmentMockedConstructionRule = constructionRule<ReporterEnvironment>()
    private val reporterEnvironment: ReporterEnvironment by reporterEnvironmentMockedConstructionRule

    @get:Rule
    val crashReporterContextMockedConstructionRule = constructionRule<CrashReporterContext>()

    private val crashReporterFieldsProvider by setUp {
        CrashReporterFieldsProvider(processConfiguration, errorEnvironment, reportsHandler, config)
    }

    @Test
    fun reportsHandler() {
        assertThat(crashReporterFieldsProvider.reportsHandler).isEqualTo(reportsHandler)
    }

    @Test
    fun reporterEnvironment() {
        assertThat(crashReporterFieldsProvider.reporterEnvironment).isEqualTo(reporterEnvironment)

        assertThat(reporterEnvironmentMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterEnvironmentMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(processConfiguration, counterConfiguration, errorEnvironment, userProfileId)

        assertThat(counterConfigurationMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(counterConfigurationMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(config, CounterConfigurationReporterType.CRASH)
    }

    @Test
    fun crashReporterContext() {
        assertThat(crashReporterFieldsProvider.crashReporterContext)
            .isEqualTo(crashReporterContextMockedConstructionRule.constructionMock.constructed().first())
        assertThat(crashReporterContextMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(crashReporterContextMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
