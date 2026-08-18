package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceConnectionTimeoutException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.reflect.InvocationTargetException

internal class ReflectionAdvIdProviderTest : CommonTest() {

    private val providerName = "test"
    private val context = mock<Context>()
    private val advIdentifiersProviderReflection = mock<AdvIdentifiersProviderReflection>()
    private val extractor by setUp { ReflectionAdvIdExtractor(providerName, advIdentifiersProviderReflection) }

    @Test
    fun `extractAdTrackingInfo returns parsed result on success`() {
        val expected = mock<AdTrackingInfoResult>()
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName)) doReturn expected

        assertThat(extractor.extractAdTrackingInfo(context)).isSameAs(expected)
    }

    @Test
    fun `extractAdTrackingInfo returns unavailable when parser returns null`() {
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName)) doReturn null

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("mErrorExplanation", "identifier provider returned no result")
            .checkAll()
    }

    @Test
    fun `extractAdTrackingInfo retries temporary service failure and succeeds`() {
        val expected = mock<AdTrackingInfoResult>()
        val retryStrategy = TimesBasedRetryStrategy(2, 0)
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(InvocationTargetException(AdvIdServiceConnectionTimeoutException("timed out")))
            .thenReturn(expected)

        assertThat(extractor.extractAdTrackingInfo(context, retryStrategy)).isSameAs(expected)
        verify(advIdentifiersProviderReflection, times(2)).requestIdentifiers(context, providerName)
    }

    @Test
    fun `extractAdTrackingInfo returns unavailable after temporary failures exhaust retry limit`() {
        val retryStrategy = TimesBasedRetryStrategy(2, 0)
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(InvocationTargetException(AdvIdServiceConnectionTimeoutException("timed out")))

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("mErrorExplanation", "service connection timed out")
            .checkAll()
        verify(advIdentifiersProviderReflection, times(2)).requestIdentifiers(context, providerName)
    }

    @Test
    fun `extractAdTrackingInfo does not retry terminal service failure`() {
        val retryStrategy = TimesBasedRetryStrategy(3, 0)
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(InvocationTargetException(AdvIdServiceNotFoundException("not found")))

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, retryStrategy))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("mErrorExplanation", "service not found")
            .checkAll()
        verify(advIdentifiersProviderReflection).requestIdentifiers(context, providerName)
    }

    @Test
    fun `extractAdTrackingInfo returns unknown for unexpected reflection failure`() {
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(IllegalAccessException("details must not be exposed"))

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, TimesBasedRetryStrategy(3, 0)))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.UNKNOWN)
            .checkField("mErrorExplanation", "identifier provider invocation failed (IllegalAccessException)")
            .checkAll()
        verify(advIdentifiersProviderReflection).requestIdentifiers(context, providerName)
    }

    @Test
    fun `extractAdTrackingInfo returns unavailable when provider module is absent`() {
        whenever(advIdentifiersProviderReflection.requestIdentifiers(context, providerName))
            .thenThrow(ClassNotFoundException("module is absent"))

        ObjectPropertyAssertions(extractor.extractAdTrackingInfo(context, TimesBasedRetryStrategy(3, 0)))
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("mErrorExplanation", "identifier provider module not found")
            .checkAll()
        verify(advIdentifiersProviderReflection).requestIdentifiers(context, providerName)
    }
}
