package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.RetryStrategy
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.lang.reflect.InvocationTargetException

internal class ReflectionAdvIdProviderTest : CommonTest() {

    private val providerName = "test"
    private val context = mock<Context>()
    private val advIdentifiersProviderReflection = mock<AdvIdentifiersProviderReflection> {
        on { isAvailable() } doReturn true
    }

    private val extractor = ReflectionAdvIdExtractor(
        providerName,
        advIdentifiersProviderReflection
    )

    @Test
    fun `extractAdTrackingInfo returns parsed result on success`() {
        val expected = mock<AdTrackingInfoResult>()
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenReturn(expected)

        assertThat(extractor.extractAdTrackingInfo(context)).isSameAs(expected)
    }

    @Test
    fun `extractAdTrackingInfo returns unavailable when parser returns null`() {
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenReturn(null)

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("mErrorExplanation", "provider $providerName is not available")
            .checkAll()
    }

    @Test
    fun `extractAdTrackingInfo retries and succeeds on second attempt`() {
        val expected = mock<AdTrackingInfoResult>()
        val retryStrategy = TimesBasedRetryStrategy(2, 0)

        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(RuntimeException("first attempt"))
            .thenReturn(expected)

        assertThat(extractor.extractAdTrackingInfo(context, retryStrategy)).isSameAs(expected)
    }

    @Test
    fun `extractAdTrackingInfo returns error after all retries exhausted with Throwable`() {
        val retryStrategy = TimesBasedRetryStrategy(2, 0)
        val message = "some error message"

        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(RuntimeException(message))

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.UNKNOWN)
            .checkField("mErrorExplanation", "exception while fetching $providerName adv_id: $message")
            .checkAll()
    }

    @Test
    fun `extractAdTrackingInfo handles InvocationTargetException`() {
        val retryStrategy = TimesBasedRetryStrategy(1, 0)
        val targetMessage = "target exception message"
        val targetException = RuntimeException(targetMessage)
        val ite = InvocationTargetException(targetException)

        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(ite)

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.UNKNOWN)
            .checkField("mErrorExplanation", "exception while fetching $providerName adv_id: $targetMessage")
            .checkAll()
    }

    @Test
    fun `extractAdTrackingInfo returns unavailable when provider class not found`() {
        val retryStrategy = mock<RetryStrategy>()
        val unavailableReflection = mock<AdvIdentifiersProviderReflection> {
            on { isAvailable() } doReturn false
        }
        val extractor = ReflectionAdvIdExtractor(providerName, unavailableReflection)

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField(
                "mErrorExplanation",
                "Module io.appmetrica.analytics:analytics-identifiers does not exist"
            )
            .checkAll()

        verifyNoInteractions(retryStrategy)
    }
}
