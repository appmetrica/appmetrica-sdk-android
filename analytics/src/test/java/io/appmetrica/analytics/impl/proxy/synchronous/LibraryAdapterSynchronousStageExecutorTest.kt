package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

class LibraryAdapterSynchronousStageExecutorTest : CommonTest() {

    private val context: Context = mock()

    private val appMetricaFacade: AppMetricaFacade = mock()

    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock {
        on { getInitializedImpl(context, true) } doReturn appMetricaFacade
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn logger
    }

    private val synchronousStageExecutor: LibraryAdapterSynchronousStageExecutor by setUp {
        LibraryAdapterSynchronousStageExecutor(appMetricaFacadeProvider)
    }

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context)
        inOrder(
            ClientServiceLocator.getInstance().contextAppearedListener,
            logger,
            ClientServiceLocator.getInstance().sessionsTrackingManager,
            appMetricaFacade
        ) {
            verify(ClientServiceLocator.getInstance().contextAppearedListener).onProbablyAppeared(context)
            verify(logger).info("Session autotracking enabled")
            verify(ClientServiceLocator.getInstance().sessionsTrackingManager).startWatchingIfNotYet()
            verify(appMetricaFacade).activateCore(null)
        }
    }
}
