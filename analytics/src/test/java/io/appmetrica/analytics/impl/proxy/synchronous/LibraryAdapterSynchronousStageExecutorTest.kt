package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Rule
import org.junit.Test
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
