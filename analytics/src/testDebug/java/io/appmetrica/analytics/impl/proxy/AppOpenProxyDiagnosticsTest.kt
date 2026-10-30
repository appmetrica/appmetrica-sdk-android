package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.proxy.synchronous.ReporterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.analytics.impl.proxy.validation.ReporterBarrier
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
internal class AppOpenProxyDiagnosticsTest(
    @Suppress("UNUSED_PARAMETER") description: String,
    private val deeplink: String?
) : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val context: Context = mock()
    private val executor: IHandlerExecutor = mock()
    private val provider: AppMetricaFacadeProvider = mock()
    private val facade: AppMetricaFacade = mock()
    private val mainReporterProvider: MainReporterApiConsumerProvider = mock()
    private val mainReporter: MainReporter = mock()
    private val reporter: IReporterExtended = mock()
    private val mainSynchronousStage: SynchronousStageExecutor = mock()
    private val reporterSynchronousStage: ReporterSynchronousStageExecutor = mock()
    private val logger: PublicLogger = mock()
    private val runnableCaptor = argumentCaptor<Runnable>()
    private val reporterApiKey = TestsData.generateApiKey()

    @Before
    fun setUp() {
        whenever(clientServiceLocatorRule.clientExecutorProvider.defaultExecutor).thenReturn(executor)
        whenever(provider.isActivated).thenReturn(true)
        whenever(provider.peekInitializedImpl()).thenReturn(facade)
        whenever(provider.getInitializedImpl(context)).thenReturn(facade)
        whenever(facade.mainReporterApiConsumerProvider).thenReturn(mainReporterProvider)
        whenever(mainReporterProvider.mainReporter).thenReturn(mainReporter)
        whenever(facade.getReporter(any<ReporterConfig>())).thenReturn(reporter)
    }

    @Test
    fun `main invalid input queues only diagnostic`() {
        val proxy = AppMetricaProxy(
            provider,
            Barrier(provider),
            SilentActivationValidator(provider),
            mock(),
            mainSynchronousStage,
            mock(),
            mock(),
            mock()
        )
        mockStatic(LoggerStorage::class.java).use { storage ->
            whenever(LoggerStorage.getMainPublicOrAnonymousLogger()).thenReturn(logger)

            proxy.reportAppOpen(deeplink)

            verify(executor).post(runnableCaptor.capture())
            verifyNoMoreInteractions(executor)
            storage.verifyNoInteractions()
            verifyNoInteractions(logger, mainSynchronousStage, mainReporter)
            (runnableCaptor.firstValue as SafeRunnable).runSafety()

            storage.verify { LoggerStorage.getMainPublicOrAnonymousLogger() }
            storage.verifyNoMoreInteractions()
            verify(logger).info("Ignoring null, empty or blank deeplink in reportAppOpen")
            verifyNoMoreInteractions(logger)
            verifyNoInteractions(mainSynchronousStage, mainReporter)
        }
    }

    @Test
    fun `reporter invalid input queues only diagnostic for its API key`() {
        val proxy = ReporterExtendedProxy(
            context,
            ReporterBarrier(),
            provider,
            reporterSynchronousStage,
            ReporterConfig.newConfigBuilder(reporterApiKey).build(),
            mock()
        )
        mockStatic(LoggerStorage::class.java).use { storage ->
            whenever(LoggerStorage.getOrCreatePublicLogger(reporterApiKey)).thenReturn(logger)

            proxy.reportAppOpen(deeplink)

            verify(executor).post(runnableCaptor.capture())
            verifyNoMoreInteractions(executor)
            storage.verifyNoInteractions()
            verifyNoInteractions(logger, reporterSynchronousStage, reporter)
            runnableCaptor.firstValue.run()

            storage.verify { LoggerStorage.getOrCreatePublicLogger(reporterApiKey) }
            storage.verifyNoMoreInteractions()
            verify(logger).info("Ignoring null, empty or blank deeplink in reportAppOpen")
            verifyNoMoreInteractions(logger)
            verifyNoInteractions(reporterSynchronousStage, reporter)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("null", null),
            arrayOf("empty", ""),
            arrayOf("space", " "),
            arrayOf("ASCII whitespace", "\t\r\n"),
            arrayOf("non-breaking space U+00A0", "\u00a0"),
            arrayOf("em space U+2003", "\u2003")
        )
    }
}
