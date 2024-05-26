package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class AppMetricaServiceCoreImplFirstCreateTaskLauncherTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val firstTask = mock<Runnable>()
    private val secondTask = mock<Runnable>()
    private val tasks = listOf(firstTask, secondTask)

    private val activationBarrierCallbackCaptor = argumentCaptor<ActivationBarrierCallback>()

    private lateinit var coreImplFirstCreateTaskLauncher: CoreImplFirstCreateTaskLauncher

    @Before
    fun setUp() {
        coreImplFirstCreateTaskLauncher = CoreImplFirstCreateTaskLauncher(tasks)
    }

    @Test
    fun run() {
        coreImplFirstCreateTaskLauncher.run()
        val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor
        verify(GlobalServiceLocator.getInstance().activationBarrier)
            .subscribe(
                eq(TimeUnit.SECONDS.toMillis(10)),
                eq(executor),
                activationBarrierCallbackCaptor.capture()
            )
        verifyNoMoreInteractions(firstTask, secondTask)
        activationBarrierCallbackCaptor.firstValue.onWaitFinished()
        verify(firstTask).run()
        verify(secondTask).run()
    }
}
