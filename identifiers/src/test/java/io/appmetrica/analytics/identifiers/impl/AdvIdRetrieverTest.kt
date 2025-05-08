package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdvIdRetrieverTest : CommonTest() {

    @get:Rule
    val rule: MockitoRule = MockitoJUnit.rule()

    val data = Bundle()

    private val context: Context = mock()
    private val advIdProvider: AdvIdProvider = mock()
    private val advIdResult: AdvIdResult = mock()

    @Before
    fun setUp() {
        doReturn(advIdResult).whenever(advIdProvider).getAdTrackingInfo(context)
        doReturn(data).whenever(advIdResult).toBundle()
    }

    @Test
    fun idRetrieving() {
        val provider = "sdijfof"
        val retriever = AdvIdRetriever(mapOf(provider to advIdProvider))

        assertThat(retriever.requestId(context, provider)).isSameAs(data)
    }
}
