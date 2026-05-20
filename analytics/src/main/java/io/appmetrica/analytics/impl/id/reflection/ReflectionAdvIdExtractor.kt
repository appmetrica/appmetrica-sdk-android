package io.appmetrica.analytics.impl.id.reflection

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.AdvIdExtractor
import io.appmetrica.analytics.impl.id.NoRetriesStrategy
import io.appmetrica.analytics.impl.id.RetryStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.lang.reflect.InvocationTargetException

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
        if (!advIdentifiersProviderReflection.isAvailable()) {
            val errorMessage = "Module io.appmetrica.analytics:analytics-identifiers does not exist"
            DebugLogger.info(tag, "[$provider] $errorMessage")
            return AdTrackingInfoResult(
                null,
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                errorMessage
            )
        }

        var result: AdTrackingInfoResult? = null
        retryStrategy.reset()
        while (retryStrategy.nextAttempt()) {
            try {
                val adTrackingInfo = advIdentifiersProviderReflection.requestIdentifiers(context, provider)
                if (adTrackingInfo != null) {
                    return adTrackingInfo
                } else {
                    DebugLogger.error(tag, "ad tracking info is null")
                    return AdTrackingInfoResult(
                        null,
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        "provider $provider is not available"
                    )
                }
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "can't fetch adv id")
                val message = (e as? InvocationTargetException)?.targetException?.message ?: e.message
                result = AdTrackingInfoResult(
                    null,
                    IdentifierStatus.UNKNOWN,
                    "exception while fetching $provider adv_id: $message"
                )
            }
            try {
                Thread.sleep(retryStrategy.timeout.toLong())
            } catch (_: InterruptedException) {
            }
        }
        return result ?: AdTrackingInfoResult()
    }
}
