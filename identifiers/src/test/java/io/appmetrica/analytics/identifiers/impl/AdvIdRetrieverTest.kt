package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.os.Bundle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdvIdRetrieverTest {

    @get:Rule
    val rule: MockitoRule = MockitoJUnit.rule()

    val data = Bundle()

    @Mock
    lateinit var context: Context

    @Mock
    internal lateinit var advIdProvider: AdvIdProvider

    @Mock
    internal lateinit var advIdResult: AdvIdResult

    @Before
    fun setUp() {
        doReturn(advIdResult).whenever(advIdProvider).getAdTrackingInfo(context)
        doReturn(data).whenever(advIdResult).toBundle()
    }

    @Test
    fun testIdRetrieving() {
        val provider = "sdijfof"
        val retriever = AdvIdRetriever(mapOf(provider to advIdProvider))

        assertThat(retriever.requestId(context, provider)).isSameAs(data)
    }
}
