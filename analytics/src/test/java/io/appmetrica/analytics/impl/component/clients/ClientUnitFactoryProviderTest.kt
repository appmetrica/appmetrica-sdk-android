package io.appmetrica.analytics.impl.component.clients

import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ClientUnitFactoryProviderTest : CommonTest() {

    private val clientDescription: ClientDescription = mock()

    @get:Rule
    val mainCommutationClientUnitFactoryRule = constructionRule<MainCommutationClientUnitFactory>()

    @get:Rule
    val selfDiagnosticMainClientUnitFactoryRule = constructionRule<SelfDiagnosticMainClientUnitFactory>()

    @get:Rule
    val selfDiagnosticReporterClientUnitFactoryRule = constructionRule<SelfDiagnosticReporterClientUnitFactory>()

    @get:Rule
    val reporterClientUnitFactoryRule = constructionRule<ReporterClientUnitFactory<*>>()

    @get:Rule
    val mainReporterClientFactoryRule = constructionRule<MainReporterClientFactory>()

    @get:Rule
    val reporterComponentUnitFactoryRule = constructionRule<ReporterComponentUnitFactory>()
    private val reporterComponentUnitFactory by reporterComponentUnitFactoryRule

    @get:Rule
    val selfSdkReporterComponentUnitFactoryRule = constructionRule<SelfSdkReporterComponentUnitFactory>()
    private val selfSdkReporterComponentUnitFactory by selfSdkReporterComponentUnitFactoryRule

    private val provider by setUp { ClientUnitFactoryProvider() }

    @Test
    fun `getClientUnitFactory for COMMUTATION`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.COMMUTATION)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(mainCommutationClientUnitFactoryRule.constructionMock.constructed().first())
        assertThat(mainCommutationClientUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainCommutationClientUnitFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for SELF_DIAGNOSTIC_MAIN`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(selfDiagnosticMainClientUnitFactoryRule.constructionMock.constructed().first())
        assertThat(selfDiagnosticMainClientUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfDiagnosticMainClientUnitFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for SELF_DIAGNOSTIC_MANUAL`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(selfDiagnosticReporterClientUnitFactoryRule.constructionMock.constructed().first())
        assertThat(selfDiagnosticReporterClientUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfDiagnosticReporterClientUnitFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for MANUAL`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.MANUAL)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(reporterClientUnitFactoryRule.constructionMock.constructed().first())
        assertThat(reporterClientUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterClientUnitFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(reporterComponentUnitFactory)

        assertThat(reporterComponentUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterComponentUnitFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for SELF_SDK`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.SELF_SDK)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(reporterClientUnitFactoryRule.constructionMock.constructed().first())
        assertThat(reporterClientUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterClientUnitFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(selfSdkReporterComponentUnitFactory)

        assertThat(selfSdkReporterComponentUnitFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfSdkReporterComponentUnitFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for MAIN`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.MAIN)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(mainReporterClientFactoryRule.constructionMock.constructed().first())
        assertThat(mainReporterClientFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterClientFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun `getClientUnitFactory for CRASH`() {
        whenever(clientDescription.reporterType).thenReturn(CounterConfigurationReporterType.CRASH)
        assertThat(provider.getClientUnitFactory(clientDescription))
            .isEqualTo(mainReporterClientFactoryRule.constructionMock.constructed().first())
        assertThat(mainReporterClientFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterClientFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun checkCoverage() {
        // If failed, add new type here and write unit test
        assertThat(CounterConfigurationReporterType.values()).containsExactlyInAnyOrder(
            CounterConfigurationReporterType.COMMUTATION,
            CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN,
            CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL,
            CounterConfigurationReporterType.MANUAL,
            CounterConfigurationReporterType.SELF_SDK,
            CounterConfigurationReporterType.MAIN,
            CounterConfigurationReporterType.CRASH
        )
    }
}
