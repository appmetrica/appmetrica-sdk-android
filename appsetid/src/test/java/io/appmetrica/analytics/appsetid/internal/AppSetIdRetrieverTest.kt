package io.appmetrica.analytics.appsetid.internal

import android.content.Context
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppSetIdRetrieverTest : CommonTest() {

    private val context: Context = mock()
    private val appSetIdListener: AppSetIdListener = mock()
    private val appSetIdClient: AppSetIdClient = mock()
    private val task: Task<AppSetIdInfo> = mock()

    private val listenerCaptor = argumentCaptor<OnCompleteListener<AppSetIdInfo>>()
    private val appSetIdRetriever = AppSetIdRetriever()

    @get:Rule
    val sAppSet = MockedStaticRule(AppSet::class.java)

    @Before
    fun setUp() {
        whenever(AppSet.getClient(context)).thenReturn(appSetIdClient)
        whenever(appSetIdClient.appSetIdInfo).thenReturn(task)
    }

    @Test
    fun retrieveAppSetIdLaunchesTask() {
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(any<OnCompleteListener<AppSetIdInfo>>())
    }

    @Test
    fun retrieveAppSetIdSuccess() {
        val id = "666-555"
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.result).thenReturn(AppSetIdInfo(id, AppSetIdInfo.SCOPE_DEVELOPER))
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.firstValue.onComplete(task)
        verify(appSetIdListener).onAppSetIdRetrieved(id, AppSetIdScope.DEVELOPER)
    }

    @Test
    fun retrieveAppSetIdFailure() {
        val ex = RuntimeException()
        whenever(task.isSuccessful).thenReturn(false)
        whenever(task.exception).thenReturn(ex)
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.firstValue.onComplete(task)
        verify(appSetIdListener).onFailure(ex)
        verify(task, never()).result
    }
}
