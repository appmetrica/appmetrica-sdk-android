package io.appmetrica.analytics.impl.startup.executor

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class RegularStartupExecutorTest : CommonTest() {
    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val networkTask: NetworkTask = mock()
    private val startupUnit: StartupUnit = mock {
        on { getOrCreateStartupTaskIfRequired() } doReturn networkTask
    }

    private val executor: RegularStartupExecutor by setUp { RegularStartupExecutor(startupUnit) }

    @Test
    fun `sendStartupIfRequired - no startup without task`() {
        whenever(startupUnit.getOrCreateStartupTaskIfRequired()).thenReturn(null)
        executor.sendStartupIfRequired()
        verifyNoInteractions(GlobalServiceLocator.getInstance().networkCore)
    }

    @Test
    fun sendStartupIfRequired() {
        executor.sendStartupIfRequired()
        verify(GlobalServiceLocator.getInstance().networkCore).startTask(networkTask)
    }
}
