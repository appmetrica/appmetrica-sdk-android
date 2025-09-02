package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ModulesSynchronousStageExecutorContextTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }

    private val contextAppearedListener: ContextAppearedListener by setUp {
        ClientServiceLocator.getInstance().contextAppearedListener
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val synchronousStageExecutor by setUp {
        ModulesSynchronousStageExecutor()
    }

    @Test
    fun getReporter() {
        val apiKey = "some_key"

        synchronousStageExecutor.getReporter(context, apiKey)
        verify(contextAppearedListener).onProbablyAppeared(applicationContext)
    }

    @Test
    fun subscribeForAutoCollectedData() {
        synchronousStageExecutor.subscribeForAutoCollectedData(context, "apiKey")
        verify(contextAppearedListener).onProbablyAppeared(applicationContext)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(
            ModulesSynchronousStageExecutor::class.java,
            ModulesSynchronousStageExecutorContextTest::class.java
        )
    }
}
