package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider
import io.appmetrica.analytics.impl.id.RetryStrategy
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReflectionAdvIdProviderTest : CommonTest() {

    @get:Rule
    val advIdentifiersProvider = MockedStaticRule(AdvIdentifiersProvider::class.java)

    private val advIdentifiersProviderClass = "io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider"

    @get:Rule
    val reflectiveUtilsStaticMockedRule = staticRule<ReflectionUtils> {
        on { ReflectionUtils.detectClassExists(advIdentifiersProviderClass) } doReturn true
    }

    private val retryStrategy = mock<RetryStrategy>()

    private val context = mock<Context>()
    private val parser = mock<ReflectionAdvIdParser>()

    private val providerName = "test"

    private val extractor = ReflectionAdvIdExtractor(providerName, parser)

    @Test
    fun getData() {
        val resultBundle = mock<Bundle>()
        val result = mock<AdTrackingInfoResult>()
        stubbing(advIdentifiersProvider.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doReturn resultBundle
        }

        doReturn(result).whenever(parser).fromBundle(resultBundle)

        assertThat(extractor.extractAdTrackingInfo(context)).isSameAs(result)
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

        assertThat(extractor.extractAdTrackingInfo(context, timesBasedRetryStrategy)).isSameAs(result)
    }

    @Test
    fun noDataWithRetry() {
        val timesBasedRetryStrategy = TimesBasedRetryStrategy(2, 1)
        val message = "some error message"
        stubbing(advIdentifiersProvider.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doThrow RuntimeException(message)
        }

        val result = extractor.extractAdTrackingInfo(context, timesBasedRetryStrategy)

        val assertions = ObjectPropertyAssertions(result)

        assertions.checkField("mAdTrackingInfo", null as AdTrackingInfo?)
        assertions.checkField("mStatus", IdentifierStatus.UNKNOWN)
        assertions.checkField("mErrorExplanation", "exception while fetching $providerName adv_id: $message")

        assertions.checkAll()
    }

    @Test
    fun noAdvIdProviderClass() {
        whenever(ReflectionUtils.detectClassExists(advIdentifiersProviderClass)).thenReturn(false)

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField(
                "mErrorExplanation",
                "Module io.appmetrica.analytics:analytics-identifiers does not exist"
            )
            .checkAll()

        verifyNoInteractions(retryStrategy)

        advIdentifiersProvider.staticMock.verify(
            { AdvIdentifiersProvider.requestIdentifiers(any(), any()) },
            never()
        )
    }
}
