package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.AnrListener
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.IUnhandledSituationReporter
import io.appmetrica.analytics.impl.MainReporterComponents
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.util.UUID

internal class MainReporterAnrControllerTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val mainReporterComponents: MainReporterComponents = mock()
    private val mainReporterConsumer: IUnhandledSituationReporter = mock()

    @get:Rule
    val libraryAnrListenerMockedConstructionRule = constructionRule<LibraryAnrListener>()
    private val libraryAnrListener: LibraryAnrListener by libraryAnrListenerMockedConstructionRule

    @get:Rule
    val anrMonitorMockedConstructionRule = constructionRule<ANRMonitor>()
    private val anrMonitor: ANRMonitor by anrMonitorMockedConstructionRule

    private val mainReporterAnrController: MainReporterAnrController by setUp {
        MainReporterAnrController(mainReporterComponents, mainReporterConsumer)
    }

    @Test
    fun anrMonitor() {
        assertThat(anrMonitorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(anrMonitorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(libraryAnrListener)
    }

    @Test
    fun anrListener() {
        assertThat(libraryAnrListenerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(libraryAnrListenerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporterComponents, mainReporterConsumer)
    }

    @Test
    fun `update config with default config`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        mainReporterAnrController.updateConfig(config)
        verify(anrMonitor).stopMonitoring()
    }

    @Test
    fun `update config with enabled`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withAnrMonitoring(true).build()
        mainReporterAnrController.updateConfig(config)
        verify(anrMonitor).startMonitoring(DefaultValues.DEFAULT_ANR_TICKS_COUNT)
    }

    @Test
    fun `update config with disabled`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withAnrMonitoring(false).build()
        mainReporterAnrController.updateConfig(config)
        verify(anrMonitor).stopMonitoring()
    }

    @Test
    fun `update config with custom timeout`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withAnrMonitoringTimeout(100).build()
        mainReporterAnrController.updateConfig(config)
        verify(anrMonitor).stopMonitoring()
    }

    @Test
    fun `update config with enabled and custom timeout`() {
        val timeout = 6
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withAnrMonitoring(true)
            .withAnrMonitoringTimeout(timeout)
            .build()
        mainReporterAnrController.updateConfig(config)
        verify(anrMonitor).startMonitoring(timeout)
    }

    @Test
    fun `enableAnrMonitoring with default timeout`() {
        mainReporterAnrController.enableAnrMonitoring()
        verify(anrMonitor).startMonitoring(DefaultValues.DEFAULT_ANR_TICKS_COUNT)
    }

    @Test
    fun `enableAnrMonitoring with custom timeout`() {
        val timeout = 35
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withAnrMonitoringTimeout(timeout).build()
        mainReporterAnrController.updateConfig(config)
        mainReporterAnrController.enableAnrMonitoring()
        verify(anrMonitor).startMonitoring(timeout)
    }

    @Test
    fun registerListener() {
        val listener: AnrListener = mock()
        mainReporterAnrController.registerListener(listener)
        val anrMonitorListener = argumentCaptor<ANRMonitor.Listener>()
        verify(anrMonitor).subscribe(anrMonitorListener.capture())
        anrMonitorListener.lastValue.onAppNotResponding()
        verify(listener).onAppNotResponding()
    }
}
