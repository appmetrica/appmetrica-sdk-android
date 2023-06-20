package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.appsetid.internal.IAppSetIdRetriever
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.same
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppSetIdGetterTest : CommonTest() {

    private lateinit var context: Context
    @Mock
    private lateinit var appSetIdRetriever: IAppSetIdRetriever
    private lateinit var appSetIdGetter: AppSetIdGetter

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        appSetIdGetter = AppSetIdGetter(context, appSetIdRetriever)
    }

    @Test
    fun getAppSetIdSuccess() {
        val id = "555-666"
        val scope = AppSetIdScope.APP
        doAnswer {
            (it.arguments[1] as AppSetIdListener).onAppSetIdRetrieved(id, scope)
        }.`when`(appSetIdRetriever).retrieveAppSetId(any<Context>(), any<AppSetIdListener>())
        assertThat(appSetIdGetter.getAppSetId()).isEqualTo(AppSetId(id, scope))
    }

    @Test
    fun getAppSetIdFailure() {
        `when`(appSetIdRetriever.retrieveAppSetId(same(context), any<AppSetIdListener>())).thenAnswer {
            (it.arguments[1] as AppSetIdListener).onFailure(null)
        }
        assertThat(appSetIdGetter.getAppSetId()).isEqualTo(AppSetId(null, AppSetIdScope.UNKNOWN))
    }

    @Test
    fun getAppSetIdCallbackNotTriggered() {
        val start = System.currentTimeMillis()
        assertThat(appSetIdGetter.getAppSetId()).isEqualTo(AppSetId(null, AppSetIdScope.UNKNOWN))
        val end = System.currentTimeMillis()
        assertThat(end - start).isGreaterThanOrEqualTo(20000)
    }

    @Test
    fun getAppSetIdTwice() {
        val id = "555-666"
        val scope = AppSetIdScope.APP
        `when`(appSetIdRetriever.retrieveAppSetId(same(context), any<AppSetIdListener>())).thenAnswer {
            (it.arguments[1] as AppSetIdListener).onAppSetIdRetrieved(id, scope)
        }
        val result = appSetIdGetter.getAppSetId()
        clearInvocations(appSetIdRetriever)
        assertThat(appSetIdGetter.getAppSetId()).isSameAs(result)
        verifyNoInteractions(appSetIdRetriever)
    }
}
