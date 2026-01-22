package io.appmetrica.analytics.idsync.impl

import android.net.Uri
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import java.util.concurrent.TimeUnit

internal class IdSyncResultRealtimeReporter(
    private val serviceContext: ServiceContext,
    private val reportingUrl: String
) : IdSyncResultReporter {

    private val tag = "[IdSyncResultRealtimeReporter]"

    private val advId = "adv_id"
    private val huaweiOaid = "oaid"
    private val yandexAdvId = "yandex_adv_id"
    private val uuid = "uuid"
    private val deviceId = "deviceid"
    private val appSetId = "app_set_id"

    private val maxRetryDuration = TimeUnit.SECONDS.toMillis(60)
    private val initialRetryDelayMs = 1000L

    private val timeProvider = SystemTimeProvider()
    private val requestSender = IdSyncResultRequestSender(serviceContext)

    override fun reportResult(value: String, sdkIdentifiers: SdkIdentifiers) {
        val platformIdentifiers = serviceContext.platformIdentifiers
        val advIdentifiers = platformIdentifiers.advIdentifiersProvider.identifiers

        val url = Uri.parse(reportingUrl).buildUpon().apply {
            advIdentifiers.google.mAdTrackingInfo?.let { appendQueryParameter(advId, it.advId) }
            advIdentifiers.huawei.mAdTrackingInfo?.let { appendQueryParameter(huaweiOaid, it.advId) }
            advIdentifiers.yandex.mAdTrackingInfo?.let { appendQueryParameter(yandexAdvId, it.advId) }
            appendQueryParameter(uuid, sdkIdentifiers.uuid)
            appendQueryParameter(deviceId, sdkIdentifiers.deviceId)
            platformIdentifiers.appSetIdProvider.getAppSetId().id?.let { appendQueryParameter(appSetId, it) }
        }.build().toString()

        val executor = serviceContext.executorProvider.getSupportIOExecutor()
        val startTime = timeProvider.currentTimeMillis()

        executor.execute { sendWithRetry(url, value, executor, startTime, 0, initialRetryDelayMs) }
    }

    private fun sendWithRetry(
        url: String,
        value: String,
        executor: IHandlerExecutor,
        startTime: Long,
        attemptNumber: Int,
        retryDelay: Long
    ) {
        val completed = requestSender.sendRequest(url, value)
        DebugLogger.info(tag, "Attempt #$attemptNumber: success=$completed")

        if (completed) {
            DebugLogger.info(tag, "Successfully sent realtime report")
        } else {
            scheduleRetryIfNeeded(url, value, executor, startTime, attemptNumber, retryDelay)
        }
    }

    private fun scheduleRetryIfNeeded(
        url: String,
        value: String,
        executor: IHandlerExecutor,
        startTime: Long,
        attemptNumber: Int,
        currentRetryDelay: Long
    ) {
        val elapsedTime = timeProvider.currentTimeMillis() - startTime
        val nextRetryDelay = currentRetryDelay * 2

        if (elapsedTime + nextRetryDelay <= maxRetryDuration) {
            DebugLogger.info(
                tag,
                "Scheduling retry #${attemptNumber + 1} in $currentRetryDelay ms, elapsed time: $elapsedTime ms"
            )
            executor.executeDelayed(
                { sendWithRetry(url, value, executor, startTime, attemptNumber + 1, nextRetryDelay) },
                currentRetryDelay
            )
        } else {
            DebugLogger.info(
                tag,
                "Max retry duration reached ( $elapsedTime ms), stopping retries"
            )
        }
    }
}
