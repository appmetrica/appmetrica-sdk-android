package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.impl.reporter.CrashReporterContext
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

internal class CrashReporterTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val oneMoreApiKey = UUID.randomUUID().toString()
    private val reporterEnvironment: ReporterEnvironment = mock()
    private val cashReporterContext: CrashReporterContext = mock()
    private val reportsHandler: ReportsHandler = mock()

    private val crashReporterFieldsProvider: CrashReporterFieldsProvider = mock {
        on { reporterEnvironment } doReturn reporterEnvironment
        on { crashReporterContext } doReturn cashReporterContext
        on { reportsHandler } doReturn reportsHandler
    }

    private val throwable: ThrowableModel = mock()
    private val unhandledException = UnhandledException(
        throwable,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    private val firstEnvironmentKey = "Environment key #1"
    private val firstEnvironmentValue = "Environment value #1"
    private val secondEnvironmentKey = "Environment key #2"
    private val secondEnvironmentValue = "Environment value #2"

    private val config = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
        .withErrorEnvironmentValue(firstEnvironmentKey, firstEnvironmentValue)
        .withErrorEnvironmentValue(secondEnvironmentKey, secondEnvironmentValue)
        .build()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn logger
        on { LoggerStorage.getOrCreatePublicLogger(oneMoreApiKey) } doReturn logger
        on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn logger
    }

    private val crashReporter by setUp { CrashReporter(crashReporterFieldsProvider) }

    @Test
    fun constructor() {
        verify(ClientServiceLocator.getInstance().reporterLifecycleListener)!!
            .onCreateCrashReporter(cashReporterContext)
    }

    @Test
    fun `constructor if no reporter lifecycle listener`() {
        clearInvocations(ClientServiceLocator.getInstance().reporterLifecycleListener)
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(null)
        CrashReporter(crashReporterFieldsProvider)
    }

    @Test
    fun reportUnhandledException() {
        crashReporter.reportUnhandledException(unhandledException)
        verify(reportsHandler).reportCrash(unhandledException, reporterEnvironment)
        verify(logger).info("Unhandled exception received: $unhandledException")
    }

    @Test
    fun `updateConfig with errorEnvironment`() {
        crashReporter.updateConfig(config)
        verify(reporterEnvironment).putErrorEnvironmentValue(firstEnvironmentKey, firstEnvironmentValue)
        verify(reporterEnvironment).putErrorEnvironmentValue(secondEnvironmentKey, secondEnvironmentValue)
    }

    @Test
    fun `updateConfig without errorEnvironment`() {
        AppMetricaConfig.newConfigBuilder(apiKey).build()
        crashReporter.updateConfig(AppMetricaConfig.newConfigBuilder(oneMoreApiKey).build())
        verifyNoInteractions(reporterEnvironment)
    }
}
