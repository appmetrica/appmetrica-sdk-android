package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LibraryAdapterSynchronousStageExecutorContextTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }

    private val appmetricaFacade: AppMetricaFacade = mock()

    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock {
        on { getInitializedImpl(context) } doReturn appmetricaFacade
    }

    private val contextAppearedListener: ContextAppearedListener by setUp {
        ClientServiceLocator.getInstance().contextAppearedListener
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val synchronousStageExecutor by setUp {
        LibraryAdapterSynchronousStageExecutor(appMetricaFacadeProvider)
    }

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(
            LibraryAdapterSynchronousStageExecutor::class.java,
            LibraryAdapterSynchronousStageExecutorContextTest::class.java
        )
    }
}
