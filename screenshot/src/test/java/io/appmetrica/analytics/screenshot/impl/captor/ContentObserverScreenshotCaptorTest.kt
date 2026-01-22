package io.appmetrica.analytics.screenshot.impl.captor

import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.provider.MediaStore
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientExecutorProvider
import io.appmetrica.analytics.screenshot.impl.ScreenshotObserver
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
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

@RunWith(RobolectricTestRunner::class)
internal class ContentObserverScreenshotCaptorTest : CommonTest() {

    private val clientSideConfig: ClientSideContentObserverCaptorConfig = mock()
    private val clientSideScreenshotConfig: ClientSideScreenshotConfig = mock {
        on { contentObserverCaptorConfig } doReturn clientSideConfig
    }
    private val activityLifecycleRegistry: ActivityLifecycleRegistry = mock()
    private val contentResolver: ContentResolver = mock()
    private val context: Context = mock {
        on { contentResolver } doReturn contentResolver
    }
    private val handler: Handler = mock()
    private val defaultExecutor: IHandlerExecutor = mock {
        on { handler } doReturn handler
    }
    private val clientExecutorProvider: ModuleClientExecutorProvider = mock {
        on { defaultExecutor } doReturn defaultExecutor
    }
    private val clientContext: ClientContext = mock {
        on { activityLifecycleRegistry } doReturn activityLifecycleRegistry
        on { context } doReturn context
        on { clientExecutorProvider } doReturn clientExecutorProvider
    }
    private val callback: ScreenshotCaptorCallback = mock()

    private val activityLifecycleListenerCaptor = argumentCaptor<ActivityLifecycleListener>()

    @get:Rule
    val screenshotObserverRule = constructionRule<ScreenshotObserver>()

    private val captor by setUp { ContentObserverScreenshotCaptor(clientContext, callback) }

    @Test
    fun getType() {
        assertThat(captor.getType()).isEqualTo("ContentObserverScreenshotCaptor")
    }

    @Test
    fun startCapture() {
        captor.startCapture()
        verify(activityLifecycleRegistry).registerListener(any(), eq(ActivityEvent.RESUMED), eq(ActivityEvent.PAUSED))
    }

    @Test
    fun listenerOnActivityResumedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.RESUMED)

        verifyNoInteractions(contentResolver)
    }

    @Test
    fun listenerOnActivityResumedIfCaptorIsDisabled() {
        whenever(clientSideConfig.enabled).thenReturn(false)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.RESUMED)

        verifyNoInteractions(contentResolver)
    }

    @Test
    fun listenerOnActivityResumed() {
        whenever(clientSideConfig.enabled).thenReturn(true)

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.RESUMED)

        verify(contentResolver).registerContentObserver(
            eq(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            eq(true),
            same(screenshotObserverRule.constructionMock.constructed().first()),
        )
    }

    @Test
    fun listenerOnActivityResumedIfExceptionThrown() {
        whenever(clientSideConfig.enabled).thenReturn(true)
        whenever(contentResolver.registerContentObserver(any(), anyBoolean(), any())).thenThrow(RuntimeException())

        val activityListener = getActivityListener()
        captor.updateConfig(clientSideScreenshotConfig)
        activityListener.onEvent(mock(), ActivityEvent.RESUMED)

        verify(contentResolver).registerContentObserver(
            eq(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            eq(true),
            same(screenshotObserverRule.constructionMock.constructed().first()),
        )
    }

    @Test
    fun listenerOnActivityPausedIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.PAUSED)

        verify(contentResolver).unregisterContentObserver(
            screenshotObserverRule.constructionMock.constructed().first()
        )
    }

    @Test
    fun listenerOnActivityPausedIfExceptionThrown() {
        whenever(contentResolver.unregisterContentObserver(any())).thenThrow(RuntimeException())

        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.PAUSED)

        verify(contentResolver).unregisterContentObserver(any())
    }

    @Test
    fun listenerOnActivityOtherEventIfConfigIsNull() {
        val activityListener = getActivityListener()
        activityListener.onEvent(mock(), ActivityEvent.CREATED)

        verifyNoInteractions(contentResolver)
    }

    private fun getActivityListener(): ActivityLifecycleListener {
        captor.startCapture()
        verify(activityLifecycleRegistry).registerListener(
            activityLifecycleListenerCaptor.capture(),
            eq(ActivityEvent.RESUMED),
            eq(ActivityEvent.PAUSED)
        )
        return activityLifecycleListenerCaptor.firstValue
    }
}
