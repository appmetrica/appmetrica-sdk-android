package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnonymousClientActivatorTest : CommonTest() {

    private val context = mock<Context>()
    private val appMetricaFacade: AppMetricaFacade = mock()

    private val provider: AppMetricaFacadeProvider = mock {
        on { getInitializedImpl(context) }.thenReturn(appMetricaFacade)
    }

    private val sessionsTrackingManager: SessionsTrackingManager = mock()

    private val executor: IHandlerExecutor = mock()

    private val clientExecutorProvider: ClientExecutorProvider = mock {
        on { defaultExecutor }.thenReturn(executor)
    }

    private val logger: PublicLogger = mock()

    private val runnableCaptor = argumentCaptor<Runnable>()

    private val config: AppMetricaLibraryAdapterConfig = mock()
    private val configCaptor = argumentCaptor<AppMetricaLibraryAdapterConfig>()

    @get:Rule
    val loggerStorageRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getMainPublicOrAnonymousLogger() }.thenReturn(logger)
    }

    private val anonymousClientActivator by setUp {
        AnonymousClientActivator(
            provider,
            sessionsTrackingManager,
            clientExecutorProvider
        )
    }

    @Test
    fun activate() {
        anonymousClientActivator.activate(context)
        inOrder(logger, sessionsTrackingManager, appMetricaFacade, executor, provider) {
            verify(logger).info("Session autotracking enabled")
            verify(sessionsTrackingManager).startWatchingIfNotYet()
            verify(appMetricaFacade).activateCore(null)
            verify(executor).execute(runnableCaptor.capture())
            verify(provider).markActivated()
            runnableCaptor.firstValue.run()
            verify(appMetricaFacade).activateFull(configCaptor.capture())
            ObjectPropertyAssertions(configCaptor.firstValue)
                .checkFieldsAreNull("advIdentifiersTracking")
                .checkAll()
        }
    }

    @Test
    fun `activate with config`() {
        anonymousClientActivator.activate(context, config)
        inOrder(logger, sessionsTrackingManager, appMetricaFacade, executor, provider) {
            verify(logger).info("Session autotracking enabled")
            verify(sessionsTrackingManager).startWatchingIfNotYet()
            verify(appMetricaFacade).activateCore(null)
            verify(executor).execute(runnableCaptor.capture())
            verify(provider).markActivated()
            runnableCaptor.firstValue.run()
            verify(appMetricaFacade).activateFull(config)
        }
    }

    @Test
    fun `activate if activated`() {
        whenever(provider.isActivated).thenReturn(true)
        anonymousClientActivator.activate(context)
        verify(appMetricaFacade).activateCore(null)
        verify(executor).execute(runnableCaptor.capture())
        verify(provider).markActivated()
        runnableCaptor.firstValue.run()
        verify(appMetricaFacade).activateFull(configCaptor.capture())
        ObjectPropertyAssertions(configCaptor.firstValue)
            .checkFieldsAreNull("advIdentifiersTracking")
            .checkAll()
        verifyNoInteractions(sessionsTrackingManager)
    }

    @Test
    fun `activate with config if activated`() {
        whenever(provider.isActivated).thenReturn(true)
        anonymousClientActivator.activate(context, config)
        verify(appMetricaFacade).activateCore(null)
        verify(executor).execute(runnableCaptor.capture())
        verify(provider).markActivated()
        runnableCaptor.firstValue.run()
        verify(appMetricaFacade).activateFull(config)
        verifyNoInteractions(sessionsTrackingManager)
    }
}
