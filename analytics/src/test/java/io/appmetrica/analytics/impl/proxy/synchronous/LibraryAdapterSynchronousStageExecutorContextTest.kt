package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LibraryAdapterSynchronousStageExecutorContextTest : CommonTest() {

    private val context: Context = mock()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val synchronousStageExecutor by setUp {
        LibraryAdapterSynchronousStageExecutor()
    }

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context)
        verify(ClientServiceLocator.getInstance().contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun `activate with configuration`() {
        synchronousStageExecutor.activate(context, mock<AppMetricaLibraryAdapterConfig>())
        verify(ClientServiceLocator.getInstance().contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(
            LibraryAdapterSynchronousStageExecutor::class.java,
            LibraryAdapterSynchronousStageExecutorContextTest::class.java
        )
    }
}
