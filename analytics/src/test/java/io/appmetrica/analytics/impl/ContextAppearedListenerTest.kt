package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

class ContextAppearedListenerTest : CommonTest() {

    private val activityLifecycleManager: ActivityLifecycleManager = mock()
    private val selfReporter: IReporterExtended = mock()
    private val context: Context = mock()
    private val applicationContext: Context = mock()
    private val activity: Activity = mock()

    private val listenerCaptor = argumentCaptor<ActivityLifecycleListener>()
    private lateinit var contextAppearedListener: ContextAppearedListener

    @Before
    fun setUp() {
        whenever(context.applicationContext).thenReturn(applicationContext)
        whenever(applicationContext.applicationContext).thenReturn(applicationContext)
        contextAppearedListener = ContextAppearedListener(activityLifecycleManager, selfReporter)
    }

    @Test
    fun onProbablyAppeared() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).maybeInit(applicationContext)
        verify(activityLifecycleManager).registerListener(
            any(ActivityLifecycleListener::class.java),
            eq(ActivityEvent.RESUMED),
            eq(ActivityEvent.PAUSED)
        )

        contextAppearedListener.onProbablyAppeared(context)
        verifyNoMoreInteractions(activityLifecycleManager)
    }

    @Test
    fun sessionResumed() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).registerListener(
            listenerCaptor.capture(),
            eq(ActivityEvent.RESUMED),
            eq(ActivityEvent.PAUSED)
        )
        listenerCaptor.firstValue.onEvent(activity, ActivityEvent.RESUMED)
        verify(selfReporter).resumeSession()
    }

    @Test
    fun sessionPaused() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).registerListener(
            listenerCaptor.capture(),
            eq(ActivityEvent.RESUMED),
            eq(ActivityEvent.PAUSED)
        )
        listenerCaptor.firstValue.onEvent(activity, ActivityEvent.PAUSED)
        verify(selfReporter).pauseSession()
    }
}
