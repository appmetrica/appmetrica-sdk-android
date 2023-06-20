package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.coreutils.internal.services.ActivationBarrier
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.rules.coreutils.UtilityServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class AppAppMetricaServiceCoreImplFirstCreateTaskLauncherTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val utilityServiceLocatorRule = UtilityServiceLocatorRule()

    private val firstTask = mock<Runnable>()
    private val secondTask = mock<Runnable>()
    private val tasks = listOf(firstTask, secondTask)

    private val activationBarrierCallbackCaptor = argumentCaptor<ActivationBarrier.IActivationBarrierCallback>()

    private lateinit var metricaCoreImplFirstCreateTaskLauncher: MetricaCoreImplFirstCreateTaskLauncher

    @Before
    fun setUp() {
        metricaCoreImplFirstCreateTaskLauncher = MetricaCoreImplFirstCreateTaskLauncher(tasks)
    }

    @Test
    fun run() {
        metricaCoreImplFirstCreateTaskLauncher.run()
        val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor
        verify(UtilityServiceLocator.instance.activationBarrier)
            .subscribe(
                eq(TimeUnit.SECONDS.toMillis(10)),
                eq(executor),
                activationBarrierCallbackCaptor.capture()
            )
        verifyZeroInteractions(firstTask, secondTask)
        activationBarrierCallbackCaptor.firstValue.onWaitFinished()
        verify(firstTask).run()
        verify(secondTask).run()
    }
}
