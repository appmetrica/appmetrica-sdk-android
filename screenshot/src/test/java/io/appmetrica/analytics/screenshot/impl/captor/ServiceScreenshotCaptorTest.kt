package io.appmetrica.analytics.screenshot.impl.captor

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideServiceCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import java.util.concurrent.Executor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ServiceScreenshotCaptorTest : CommonTest() {

    private val clientSideConfig: ClientSideServiceCaptorConfig = mock()
    private val clientSideScreenshotConfig: ClientSideScreenshotConfig = mock {
        on { serviceCaptorConfig } doReturn clientSideConfig
    }
    private val activityLifecycleRegistry: ActivityLifecycleRegistry = mock()
    private val mainExecutor: Executor = mock()
    private val context: Context = mock {
        on { mainExecutor } doReturn mainExecutor
    }
    private val handler: Handler = mock()
    private val defaultExecutor: IHandlerExecutor = mock {
        on { handler } doReturn handler
    }
    private val clientContext: ClientContext = mock {
        on { activityLifecycleRegistry } doReturn activityLifecycleRegistry
        on { context } doReturn context
        on { defaultExecutor } doReturn defaultExecutor
    }
    private val callback: ScreenshotCaptorCallback = mock()

    private val activityLifecycleListenerCaptor = argumentCaptor<ActivityLifecycleListener>()
    private val serviceSearcherCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val systemServiceUtilsRule = staticRule<SystemServiceUtils>()

    private val captor by setUp { ServiceScreenshotCaptor(clientContext, callback) }

    @Test
    fun getType() {
        assertThat(captor.getType()).isEqualTo("ServiceScreenshotCaptor")
    }

    @Test
    fun startCapture() {
        captor.startCapture()
        verify(activityLifecycleRegistry).registerListener(any(), eq(ActivityEvent.STARTED), eq(ActivityEvent.STOPPED))
    }

    @Test
    fun listenerOnActivityStartedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.STARTED)

        verifyNoInteractions(handler)
    }

    @Test
    fun listenerOnActivityStartedIfCaptorIsDisabled() {
        whenever(clientSideConfig.enabled).thenReturn(false)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.STARTED)

        verifyNoInteractions(handler)
    }

    @Test
    fun listenerOnActivityStarted() {
        whenever(clientSideConfig.enabled).thenReturn(true)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.STARTED)

        verify(handler).postDelayed(
            any(),
            eq(0)
        )
    }

    @Test
    fun listenerOnActivityStartedIfExceptionThrown() {
        whenever(clientSideConfig.enabled).thenReturn(true)
        whenever(handler.postDelayed(any(), anyLong())).thenThrow(RuntimeException())

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.STARTED)

        verify(handler).postDelayed(
            any(),
            eq(0)
        )
    }

    @Test
    fun listenerOnActivityStoppedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.STOPPED)
    }

    @Test
    fun serviceSearcherIfStopped() {
        val activityListener = getActivityListener()
        val serviceSearcher = getServiceSearcher(activityListener)

        activityListener.onEvent(mock(), ActivityEvent.STOPPED)
        serviceSearcher.run()

        systemServiceUtilsRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun serviceSearcherIfConfigIsNull() {
        val serviceSearcher = getServiceSearcher(getActivityListener())

        captor.updateConfig(null)
        serviceSearcher.run()

        systemServiceUtilsRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun serviceSearcherIfCaptorIsDisabled() {
        val serviceSearcher = getServiceSearcher(getActivityListener())

        whenever(clientSideConfig.enabled).thenReturn(false)
        captor.updateConfig(clientSideScreenshotConfig)
        serviceSearcher.run()

        systemServiceUtilsRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun serviceSearcher() {
        whenever(clientSideConfig.enabled).thenReturn(true)
        whenever(clientSideConfig.delaySeconds).thenReturn(5)
        val serviceSearcher = getServiceSearcher(getActivityListener())

        captor.updateConfig(clientSideScreenshotConfig)
        serviceSearcher.run()

        val tryBlockCaptor = argumentCaptor<FunctionWithThrowable<ActivityManager, Unit?>>()

        systemServiceUtilsRule.staticMock.verify {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                same(context),
                eq(Context.ACTIVITY_SERVICE),
                eq("running service screenshot captor"),
                eq("ActivityManager"),
                tryBlockCaptor.capture()
            )
        }

        val servicesInfo = listOf(
            "some_service_one",
            "com.android.systemui:screenshot",
            "some_service_two",
            "com.android.systemui:screenshot",
            "some_service_three"
        ).map { processName ->
            RunningServiceInfo().also {
                it.process = processName
            }
        }
        val activityManager: ActivityManager = mock {
            on { getRunningServices(200) } doReturn servicesInfo
        }

        val tryBlock = tryBlockCaptor.firstValue
        tryBlock.apply(activityManager)

        verify(callback, times(1)).screenshotCaptured("ServiceScreenshotCaptor")
        verify(handler).postDelayed(
            same(serviceSearcher),
            eq(5000)
        )
    }

    @Test
    fun serviceSearcherIfNoScreenshotService() {
        whenever(clientSideConfig.enabled).thenReturn(true)
        whenever(clientSideConfig.delaySeconds).thenReturn(5)
        val serviceSearcher = getServiceSearcher(getActivityListener())

        captor.updateConfig(clientSideScreenshotConfig)
        serviceSearcher.run()

        val tryBlockCaptor = argumentCaptor<FunctionWithThrowable<ActivityManager, Unit?>>()

        systemServiceUtilsRule.staticMock.verify {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                same(context),
                eq(Context.ACTIVITY_SERVICE),
                eq("running service screenshot captor"),
                eq("ActivityManager"),
                tryBlockCaptor.capture()
            )
        }

        val servicesInfo = listOf(
            "some_service_one",
            "some_service_two",
            "some_service_three"
        ).map { processName ->
            RunningServiceInfo().also {
                it.process = processName
            }
        }
        val activityManager: ActivityManager = mock {
            on { getRunningServices(200) } doReturn servicesInfo
        }

        val tryBlock = tryBlockCaptor.firstValue
        tryBlock.apply(activityManager)

        verify(callback, never()).screenshotCaptured(any())
        verify(handler).postDelayed(
            same(serviceSearcher),
            eq(5000)
        )
    }

    private fun getActivityListener(): ActivityLifecycleListener {
        captor.startCapture()
        verify(activityLifecycleRegistry).registerListener(
            activityLifecycleListenerCaptor.capture(),
            eq(ActivityEvent.STARTED),
            eq(ActivityEvent.STOPPED)
        )
        return activityLifecycleListenerCaptor.firstValue
    }

    private fun getServiceSearcher(activityListener: ActivityLifecycleListener): Runnable {
        whenever(clientSideConfig.enabled).thenReturn(true)

        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.STARTED)

        verify(handler).postDelayed(
            serviceSearcherCaptor.capture(),
            eq(0)
        )

        return serviceSearcherCaptor.firstValue
    }
}
