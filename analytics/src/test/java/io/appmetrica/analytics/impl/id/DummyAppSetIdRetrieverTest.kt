package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

internal class DummyAppSetIdRetrieverTest : CommonTest() {

    private val context: Context = mock()
    private val listener: AppSetIdListener = mock()

    private val retriever = DummyAppSetIdRetriever()

    @Test
    fun retrieveAppSetId() {
        retriever.retrieveAppSetId(context, listener)
        verify(listener).onFailure(any<IllegalStateException>())
    }
}
