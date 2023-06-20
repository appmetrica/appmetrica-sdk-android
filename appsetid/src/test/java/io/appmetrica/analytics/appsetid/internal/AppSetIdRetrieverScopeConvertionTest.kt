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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class AppSetIdRetrieverScopeConvertionTest(
    private val input: Int,
    private val expected: AppSetIdScope
) {

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

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun scopeConvertion() {
        `when`(AppSet.getClient(context)).thenReturn(appSetIdClient)
        `when`(appSetIdClient.appSetIdInfo).thenReturn(task)
        `when`(task.isSuccessful).thenReturn(true)
        `when`(task.result).thenReturn(AppSetIdInfo("id", input))
        appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
        verify(task).addOnCompleteListener(listenerCaptor.capture())
        listenerCaptor.value.onComplete(task)
        verify(appSetIdListener).onAppSetIdRetrieved("id", expected)
    }
}
