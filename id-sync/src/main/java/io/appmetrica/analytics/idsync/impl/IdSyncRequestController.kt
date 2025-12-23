package io.appmetrica.analytics.idsync.impl

import android.text.TextUtils
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.idsync.impl.model.RequestAttemptResult
import io.appmetrica.analytics.idsync.impl.model.RequestState
import io.appmetrica.analytics.idsync.impl.model.RequestStateHolder
import io.appmetrica.analytics.idsync.impl.precondition.PreconditionProvider
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class IdSyncRequestController(
    private val serviceContext: ServiceContext,
    private val requestStateHolder: RequestStateHolder,
    var sdkIdentifiers: SdkIdentifiers
) : IdSyncRequestCallback {
    private val tag = "[IdSyncRequestController]"

    private val timeProvider = SystemTimeProvider()
    private val requestSender = IdSyncRequestSender(serviceContext.networkContext.sslSocketFactoryProvider, this)
    private val preconditionProvider = PreconditionProvider(serviceContext)
    private val resultHandler = IdSyncResultHandler(serviceContext)

    fun handle(requestConfig: RequestConfig) {
        if (!requestConfig.isValid()) {
            DebugLogger.warning(tag, "Request config is not valid: $requestConfig")
            return
        }
        val requestState = requestStateHolder.getRequestState(requestConfig.type)
        if (requestState == null || requestState.shouldSend(requestConfig)) {
            serviceContext.executorProvider.getSupportIOExecutor().execute {
                DebugLogger.info(tag, "Send request: $requestConfig")
                val precondition = preconditionProvider.getPrecondition(requestConfig.preconditions)
                DebugLogger.info(
                    tag,
                    "Precondition: $precondition; matchPrecondition: ${precondition.matchPrecondition()}"
                )
                if (precondition.matchPrecondition()) {
                    requestSender.sendRequest(requestConfig)
                } else {
                    DebugLogger.info(tag, "Request is not ready to send by preconditions: $requestConfig")
                }
            }
        } else {
            DebugLogger.info(tag, "Request is not ready to send: $requestConfig")
        }
    }

    override fun onResult(result: RequestResult, requestConfig: RequestConfig) {
        serviceContext.executorProvider.moduleExecutor.execute {
            DebugLogger.info(tag, "Received request result: $result")
            if (result.isCompleted) {
                DebugLogger.info(tag, "Update local state and report event")
                requestStateHolder.updateRequestState(
                    RequestState(
                        result.type,
                        timeProvider.currentTimeMillis(),
                        result.toRequestAttemptResult()
                    )
                )
                resultHandler.reportEvent(result, requestConfig, sdkIdentifiers)
            } else {
                DebugLogger.info(tag, "Request was not completed")
            }
        }
    }

    private fun RequestResult.toRequestAttemptResult() =
        if (responseCodeIsValid) RequestAttemptResult.SUCCESS else RequestAttemptResult.FAILURE

    private fun RequestState.shouldSend(config: RequestConfig): Boolean {
        val now = timeProvider.currentTimeMillis()
        val resentInterval = when (lastAttemptResult) {
            RequestAttemptResult.SUCCESS -> config.resendIntervalForValidResponse
            RequestAttemptResult.FAILURE -> config.resendIntervalForInvalidResponse
            else -> 0
        }
        val result = now - lastAttempt >= resentInterval
        DebugLogger.info(tag, "Should send request: $result for config: $config; state: $this; time: $now")
        return result
    }

    private fun RequestConfig.isValid(): Boolean =
        !TextUtils.isEmpty(type) && !TextUtils.isEmpty(url) && this.validResponseCodes.isNotEmpty()
}
