package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class DummyAppSetIdRetrieverTest : CommonTest() {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var listener: AppSetIdListener
    private val retriever = DummyAppSetIdRetriever()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun retrieveAppSetId() {
        retriever.retrieveAppSetId(context, listener)
        verify(listener).onFailure(any<IllegalStateException>())
    }
}
