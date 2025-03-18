package io.appmetrica.analytics.screenshot.impl.captor

import android.app.Activity
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import java.util.concurrent.Executor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidApiScreenshotCaptorTest : CommonTest() {

    private val activity: Activity = mock()
    private val clientSideConfig: ClientSideApiCaptorConfig = mock()
    private val clientSideScreenshotConfig: ClientSideScreenshotConfig = mock {
        on { apiCaptorConfig } doReturn clientSideConfig
    }
    private val activityLifecycleRegistry: ActivityLifecycleRegistry = mock()
    private val mainExecutor: Executor = mock()
    private val context: Context = mock {
        on { mainExecutor } doReturn mainExecutor
    }
    private val clientContext: ClientContext = mock {
        on { activityLifecycleRegistry } doReturn activityLifecycleRegistry
        on { context } doReturn context
    }
    private val callback: ScreenshotCaptorCallback = mock()

    private val activityLifecycleListenerCaptor = argumentCaptor<ActivityLifecycleListener>()
    private val screenCaptureCallbackCaptor = argumentCaptor<Activity.ScreenCaptureCallback>()

    private val captor by setUp { AndroidApiScreenshotCaptor(clientContext, callback) }

    @Test
    fun getType() {
        assertThat(captor.getType()).isEqualTo("AndroidApiScreenshotCaptor")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun startCaptureIfApiNotAchieved() {
        captor.startCapture()
        verifyNoInteractions(clientContext)
    }

    @Test
    fun startCapture() {
        captor.startCapture()
        verify(activityLifecycleRegistry).registerListener(any(), eq(ActivityEvent.STARTED), eq(ActivityEvent.STOPPED))
    }

    @Test
    fun listenerOnActivityStartedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(activity, ActivityEvent.STARTED)

        verifyNoInteractions(activity)
    }

    @Test
    fun listenerOnActivityStartedIfCaptorIsDisabled() {
        whenever(clientSideConfig.enabled).thenReturn(false)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(activity, ActivityEvent.STARTED)

        verifyNoInteractions(activity)
    }

    @Test
    fun listenerOnActivityStarted() {
        whenever(clientSideConfig.enabled).thenReturn(true)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(activity, ActivityEvent.STARTED)

        verify(activity).registerScreenCaptureCallback(
            same(mainExecutor),
            screenCaptureCallbackCaptor.capture()
        )

        val screenCaptureCallback = screenCaptureCallbackCaptor.firstValue
        screenCaptureCallback.onScreenCaptured()

        verify(callback).screenshotCaptured("AndroidApiScreenshotCaptor")
    }

    @Test
    fun listenerOnActivityStartedIfExceptionThrown() {
        whenever(clientSideConfig.enabled).thenReturn(true)
        whenever(activity.registerScreenCaptureCallback(any(), any())).thenThrow(RuntimeException())

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(activity, ActivityEvent.STARTED)

        verify(activity).registerScreenCaptureCallback(
            same(mainExecutor),
            screenCaptureCallbackCaptor.capture()
        )

        val screenCaptureCallback = screenCaptureCallbackCaptor.firstValue
        screenCaptureCallback.onScreenCaptured()

        verify(callback).screenshotCaptured("AndroidApiScreenshotCaptor")
    }

    @Test
    fun listenerOnActivityStoppedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(activity, ActivityEvent.STOPPED)

        verify(activity).unregisterScreenCaptureCallback(any())
    }

    @Test
    fun listenerOnActivityStoppedCheckCallback() {
        whenever(clientSideConfig.enabled).thenReturn(true)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(activity, ActivityEvent.STARTED)

        verify(activity).registerScreenCaptureCallback(
            same(mainExecutor),
            screenCaptureCallbackCaptor.capture()
        )

        val screenCaptureCallback = screenCaptureCallbackCaptor.firstValue

        activityListener.onEvent(activity, ActivityEvent.STOPPED)

        verify(activity).unregisterScreenCaptureCallback(screenCaptureCallback)
    }

    @Test
    fun listenerOnActivityStoppedIfExceptionThrown() {
        whenever(activity.unregisterScreenCaptureCallback(any())).thenThrow(RuntimeException())

        val activityListener = getActivityListener()
        activityListener.onEvent(activity, ActivityEvent.STOPPED)

        verify(activity).unregisterScreenCaptureCallback(any())
    }

    @Test
    fun listenerOnActivityOtherEventIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(activity, ActivityEvent.PAUSED)

        verifyNoInteractions(activity)
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
}
