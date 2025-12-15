package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.network.internal.NetworkClientBuilder
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request

internal class IdSyncRequestSender(
    private val sslSocketFactoryProvider: SslSocketFactoryProvider,
    private val requestCallback: IdSyncRequestCallback
) {
    private val tag = "[IdSyncRequestSender]"

    private val responseValueLimit = 100 * 1024

    fun sendRequest(requestConfig: RequestConfig) {
        DebugLogger.info(tag, "Send request with config: $requestConfig")
        val request = Request.Builder(requestConfig.url)
            .apply {
                requestConfig.headers.forEach { headerPair ->
                    addHeader(headerPair.key, headerPair.value.joinToString(", "))
                }
            }
            .build()

        val client = NetworkClientBuilder()
            .withSettings(
                NetworkClientSettings.Builder()
                    .withSslSocketFactory(sslSocketFactoryProvider.sslSocketFactory)
                    .withUseCaches(false)
                    .withInstanceFollowRedirects(true)
                    .withMaxResponseSize(responseValueLimit)
                    .build()
            )
            .build()

        val response = client.newCall(request).execute()

        DebugLogger.info(tag, "Received response: $response with value: ${response.responseData}")

        requestCallback.onResult(
            RequestResult(
                requestConfig.type,
                response.isCompleted,
                response.url,
                requestConfig.validResponseCodes.contains(response.code),
                response.code,
                response.responseData,
                response.headers
            )
        )
    }
}
