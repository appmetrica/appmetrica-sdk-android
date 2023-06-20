package io.appmetrica.analytics.appsetid.internal

import android.content.Context
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppSetIdRetrieverTest {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var appSetIdListener: AppSetIdListener
    @Mock
    private lateinit var appSetIdClient: AppSetIdClient
    @Mock
    private lateinit var task: Task<AppSetIdInfo>
    @Captor
    private lateinit var listenerCaptor: ArgumentCaptor<OnCompleteListener<AppSetIdInfo>>
    private val appSetIdRetriever = AppSetIdRetriever()

    @Rule
    @JvmField
    val sAppSet = MockedStaticRule(AppSet::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(AppSet.getClient(context)).thenReturn(appSetIdClient)
        `when`(appSetIdClient.appSetIdInfo).thenReturn(task)
    }

    @Test
    fun retrieveAppSetIdLaunchesTask() {
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(any<OnCompleteListener<AppSetIdInfo>>())
    }

    @Test
    fun retrieveAppSetIdSuccess() {
        val id ="666-555"
        `when`(task.isSuccessful).thenReturn(true)
        `when`(task.result).thenReturn(AppSetIdInfo(id, AppSetIdInfo.SCOPE_DEVELOPER))
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.value.onComplete(task)
        verify(appSetIdListener).onAppSetIdRetrieved(id, AppSetIdScope.DEVELOPER)
    }

    @Test
    fun retrieveAppSetIdFailure() {
        val ex= RuntimeException()
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(ex)
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.value.onComplete(task)
        verify(appSetIdListener).onFailure(ex)
        verify(task, never()).result
    }

}
