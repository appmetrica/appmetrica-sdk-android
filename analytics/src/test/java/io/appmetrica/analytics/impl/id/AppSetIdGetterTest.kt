package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.appsetid.internal.IAppSetIdRetriever
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.same
import org.mockito.kotlin.whenever

internal class AppSetIdGetterTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val appSetIdRetriever: IAppSetIdRetriever = mock()

    private val appSetIdGetter by setUp { AppSetIdGetter(context, appSetIdRetriever) }

    @Test
    fun getAppSetIdSuccess() {
        val id = "555-666"
        val scope = AppSetIdScope.APP
        doAnswer {
            (it.arguments[1] as AppSetIdListener).onAppSetIdRetrieved(id, scope)
        }.whenever(appSetIdRetriever).retrieveAppSetId(any<Context>(), any<AppSetIdListener>())
        assertThat(appSetIdGetter.getAppSetId()).isEqualTo(AppSetId(id, scope))
    }

    @Test
    fun getAppSetIdFailure() {
        whenever(appSetIdRetriever.retrieveAppSetId(same(context), any<AppSetIdListener>())).thenAnswer {
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
        whenever(appSetIdRetriever.retrieveAppSetId(same(context), any<AppSetIdListener>())).thenAnswer {
            (it.arguments[1] as AppSetIdListener).onAppSetIdRetrieved(id, scope)
        }
        val result = appSetIdGetter.getAppSetId()
        clearInvocations(appSetIdRetriever)
        assertThat(appSetIdGetter.getAppSetId()).isSameAs(result)
        verifyNoInteractions(appSetIdRetriever)
    }
}
