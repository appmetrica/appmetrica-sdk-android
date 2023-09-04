package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReflectionAdvIdProviderTest : CommonTest() {

    @get:Rule
    val advIdentifiersProvider = MockedStaticRule(AdvIdentifiersProvider::class.java)

    private val context = mock<Context>()
    private val parser = mock<ReflectionAdvIdParser>()

    private val providerName = "test"

    private val provider = ReflectionAdvIdProvider(providerName, parser)

    @Test
    fun getData() {
        val resultBundle = mock<Bundle>()
        val result = mock<AdTrackingInfoResult>()
        stubbing(advIdentifiersProvider.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doReturn resultBundle
        }

        doReturn(result).whenever(parser).fromBundle(resultBundle)

        assertThat(provider.getAdTrackingInfo(context)).isSameAs(result)
    }

    @Test
    fun getDataWithRetry() {
        val resultBundle = mock<Bundle>()
        val result = mock<AdTrackingInfoResult>()
        val timesBasedRetryStrategy = TimesBasedRetryStrategy(2, 1)

        stubbing(advIdentifiersProvider.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) }
                .thenThrow(RuntimeException())
                .thenReturn(resultBundle)
        }

        doReturn(result).whenever(parser).fromBundle(resultBundle)

        assertThat(provider.getAdTrackingInfo(context, timesBasedRetryStrategy)).isSameAs(result)
    }

    @Test
    fun noDataWithRetry() {
        val timesBasedRetryStrategy = TimesBasedRetryStrategy(2, 1)
        val message = "some error message"
        stubbing(advIdentifiersProvider.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doThrow RuntimeException(message)
        }

        val result = provider.getAdTrackingInfo(context, timesBasedRetryStrategy)

        val assertions = ObjectPropertyAssertions(result)

        assertions.checkField("mAdTrackingInfo", null as AdTrackingInfo?)
        assertions.checkField("mStatus", IdentifierStatus.UNKNOWN)
        assertions.checkField("mErrorExplanation", "exception while fetching $providerName adv_id: $message")

        assertions.checkAll()
    }
}
