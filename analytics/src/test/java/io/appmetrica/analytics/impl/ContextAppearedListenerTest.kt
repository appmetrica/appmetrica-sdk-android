package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ContextAppearedListenerTest : CommonTest() {

    @Mock
    private lateinit var activityLifecycleManager: ActivityLifecycleManager
    @Mock
    private lateinit var selfReporter: IReporterExtended
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var applicationContext: Context
    @Mock
    private lateinit var activity: Activity
    @Captor
    private lateinit var listenerCaptor: ArgumentCaptor<ActivityLifecycleManager.Listener>
    private lateinit var contextAppearedListener: ContextAppearedListener

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(context.applicationContext).thenReturn(applicationContext)
        `when`(applicationContext.applicationContext).thenReturn(applicationContext)
        contextAppearedListener = ContextAppearedListener(activityLifecycleManager, selfReporter)
    }

    @Test
    fun onProbablyAppeared() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).maybeInit(applicationContext)
        verify(activityLifecycleManager).registerListener(
            any(ActivityLifecycleManager.Listener::class.java),
            eq(ActivityLifecycleManager.ActivityEvent.RESUMED),
            eq(ActivityLifecycleManager.ActivityEvent.PAUSED)
        )

        contextAppearedListener.onProbablyAppeared(context)
        verifyNoMoreInteractions(activityLifecycleManager)
    }

    @Test
    fun sessionResumed() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).registerListener(
            listenerCaptor.capture(),
            eq(ActivityLifecycleManager.ActivityEvent.RESUMED),
            eq(ActivityLifecycleManager.ActivityEvent.PAUSED)
        )
        listenerCaptor.value.onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED)
        verify(selfReporter).resumeSession()
    }

    @Test
    fun sessionPaused() {
        contextAppearedListener.onProbablyAppeared(context)
        verify(activityLifecycleManager).registerListener(
            listenerCaptor.capture(),
            eq(ActivityLifecycleManager.ActivityEvent.RESUMED),
            eq(ActivityLifecycleManager.ActivityEvent.PAUSED)
        )
        listenerCaptor.value.onEvent(activity, ActivityLifecycleManager.ActivityEvent.PAUSED)
        verify(selfReporter).pauseSession()
    }
}
