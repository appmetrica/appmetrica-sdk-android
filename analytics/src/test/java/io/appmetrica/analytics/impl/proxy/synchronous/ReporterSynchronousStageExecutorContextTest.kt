package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ReporterSynchronousStageExecutorContextTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }

    private val synchronousStageExecutor = ReporterSynchronousStageExecutor()

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(
            ReporterSynchronousStageExecutor::class.java,
            ReporterSynchronousStageExecutorContextTest::class.java
        )
    }
}
