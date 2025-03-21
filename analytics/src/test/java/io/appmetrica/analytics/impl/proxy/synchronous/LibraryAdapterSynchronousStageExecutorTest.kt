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

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val synchronousStageExecutor: LibraryAdapterSynchronousStageExecutor by setUp {
        LibraryAdapterSynchronousStageExecutor()
    }

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context)
        inOrder(
            ClientServiceLocator.getInstance().contextAppearedListener,
            ClientServiceLocator.getInstance().anonymousClientActivator
        ) {
            verify(ClientServiceLocator.getInstance().contextAppearedListener).onProbablyAppeared(context)
            verify(ClientServiceLocator.getInstance().anonymousClientActivator).activate(context)
        }
    }
}
