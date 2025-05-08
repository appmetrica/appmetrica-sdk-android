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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class AppSetIdRetrieverScopeConvertionTest(
    private val input: Int,
    private val expected: AppSetIdScope
) : CommonTest() {

    private val context: Context = mock()
    private val appSetIdListener: AppSetIdListener = mock()
    private val appSetIdClient: AppSetIdClient = mock()
    private val task: Task<AppSetIdInfo> = mock()

    private val listenerCaptor = argumentCaptor<OnCompleteListener<AppSetIdInfo>>()
    private val appSetIdRetriever = AppSetIdRetriever()

    @get:Rule
    val sAppSet = MockedStaticRule(AppSet::class.java)

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf(AppSetIdInfo.SCOPE_APP, AppSetIdScope.APP),
                arrayOf(AppSetIdInfo.SCOPE_DEVELOPER, AppSetIdScope.DEVELOPER),
                arrayOf(12, AppSetIdScope.UNKNOWN),
            )
        }
    }

    @Test
    fun scopeConvertion() {
        whenever(AppSet.getClient(context)).thenReturn(appSetIdClient)
        whenever(appSetIdClient.appSetIdInfo).thenReturn(task)
        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.result).thenReturn(AppSetIdInfo("id", input))
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.firstValue.onComplete(task)
        verify(appSetIdListener).onAppSetIdRetrieved("id", expected)
    }
}
