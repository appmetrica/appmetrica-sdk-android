package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.AdvIdExtractor
import io.appmetrica.analytics.impl.id.NoRetriesStrategy
import io.appmetrica.analytics.impl.id.RetryStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReflectionAdvIdExtractor internal constructor(
    private val provider: String,
    private val advIdentifiersProviderReflection: AdvIdentifiersProviderReflection =
        AdvIdentifiersProviderReflection(ReflectionAdvIdParser())
) : AdvIdExtractor {

    private val tag = "[ReflectionAdvIdExtractor]"

    override fun extractAdTrackingInfo(context: Context): AdTrackingInfoResult {
        return extractAdTrackingInfo(context, NoRetriesStrategy())
    }

    override fun extractAdTrackingInfo(context: Context, retryStrategy: RetryStrategy): AdTrackingInfoResult {
        DebugLogger.info(tag, "getAdTrackingInfo. Connecting to library for %s adv_id", provider)
        retryStrategy.reset()
        var lastFailure: AdTrackingInfoResult? = null
        var shouldRetry = retryStrategy.nextAttempt()
        while (shouldRetry) {
            try {
                val adTrackingInfo = advIdentifiersProviderReflection.requestIdentifiers(context, provider)
                if (adTrackingInfo != null) {
                    return adTrackingInfo
                } else {
                    DebugLogger.error(tag, "ad tracking info is null")
                    return AdTrackingInfoResult(
                        null,
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        "identifier provider returned no result"
                    )
                }
            } catch (exception: Throwable) {
                DebugLogger.error(tag, exception, "can't fetch adv id")
                val failure = AdvIdProviderExceptionMapper.map(exception)
                lastFailure = failure.result
                shouldRetry = failure.isRetryable && retryStrategy.nextAttempt()
                if (shouldRetry) {
                    try {
                        Thread.sleep(retryStrategy.timeout.toLong())
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return failure.result
                    }
                }
            }
        }
        return lastFailure ?: AdTrackingInfoResult()
    }
}
