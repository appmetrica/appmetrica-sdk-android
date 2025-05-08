package io.appmetrica.analytics.impl.utils.executors

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClientExecutorFactoryTest : CommonTest() {

    private val clientExecutorFactory: ClientExecutorFactory by setUp { ClientExecutorFactory() }

    @Test
    fun createReportsSenderExecutor() {
        assertThat(clientExecutorFactory.createReportsSenderExecutor().looper.thread.name).startsWith("IAA-CRS")
    }

    @Test
    fun createDefaultExecutor() {
        assertThat(clientExecutorFactory.createDefaultExecutor().looper.thread.name).startsWith("IAA-CDE")
    }

    @Test
    fun createInitCoreThread() {
        val runnable = mock<Runnable>()
        val thread = clientExecutorFactory.createInitCoreThread(runnable)
        assertThat(thread.name).startsWith("IAA-INIT_CORE")
        thread.start()
        thread.join()
        verify(runnable).run()
    }
}
