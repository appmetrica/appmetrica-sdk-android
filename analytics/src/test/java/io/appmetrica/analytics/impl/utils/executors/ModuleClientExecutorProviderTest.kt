package io.appmetrica.analytics.impl.utils.executors

import android.os.Handler
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModuleClientExecutorProviderTest : CommonTest() {

    private val reportsSenderExecutor: ExecutorWrapper = mock()
    private val defaultExecutor: ExecutorWrapper = mock()
    private val handler: Handler = mock()
    private val runnable: Runnable = mock()
    private val coreInitThread: Thread = mock()

    private val clientExecutorFactory: ClientExecutorFactory = mock {
        on { createDefaultExecutor() } doReturn defaultExecutor
        on { createReportsSenderExecutor() } doReturn reportsSenderExecutor
        on { createMainHandler() } doReturn handler
        on { createInitCoreThread(runnable) } doReturn coreInitThread
    }

    private val clientExecutorProvider: ClientExecutorProvider by setUp {
        ClientExecutorProvider(clientExecutorFactory)
    }

    @Test
    fun getReportsSenderExecutor() {
        for (i in 0 until CALLS_COUNT) {
            assertThat(clientExecutorProvider.reportSenderExecutor)
                .describedAs("Attempt #%d", i)
                .isEqualTo(reportsSenderExecutor)
        }
        verify(clientExecutorFactory).createReportsSenderExecutor()
        verifyNoMoreInteractions(clientExecutorFactory)
    }

    @Test
    fun getMainHandler() {
        for (i in 0 until CALLS_COUNT) {
            assertThat(clientExecutorProvider.mainHandler)
                .describedAs("Attempt #%d", i)
                .isEqualTo(handler)
        }
        verify(clientExecutorFactory).createMainHandler()
        verifyNoMoreInteractions(clientExecutorFactory)
    }

    @Test
    fun getDefaultWorkingExecutor() {
        for (i in 0 until CALLS_COUNT) {
            assertThat(clientExecutorProvider.defaultExecutor).`as`("Attempt #%d", i)
                .isEqualTo(defaultExecutor)
        }
        verify(clientExecutorFactory).createDefaultExecutor()
        verifyNoMoreInteractions(clientExecutorFactory)
    }

    @Test
    fun getCoreInitThread() {
        assertThat(clientExecutorFactory.createInitCoreThread(runnable)).isEqualTo(coreInitThread)
    }

    companion object {
        private const val CALLS_COUNT = 10
    }
}
