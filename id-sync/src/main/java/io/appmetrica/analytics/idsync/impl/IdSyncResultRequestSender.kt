package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.network.internal.NetworkClientBuilder
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import javax.net.ssl.HttpsURLConnection

class IdSyncResultRequestSender(private val serviceContext: ServiceContext) {

    private val tag = "[IdSyncResultRequestSender]"
    private val responseValueLimit = 10 * 1024

    fun sendRequest(url: String, value: String): Boolean {
        return try {
            val request = Request.Builder(url)
                .withMethod(Request.Method.POST)
                .withBody(value.toByteArray(Charsets.UTF_8))
                .addHeader("Content-Type", "application/json")
                .build()

            val client = NetworkClientBuilder()
                .withSettings(
                    NetworkClientSettings.Builder()
                        .withSslSocketFactory(serviceContext.networkContext.sslSocketFactoryProvider.sslSocketFactory)
                        .withUseCaches(false)
                        .withInstanceFollowRedirects(true)
                        .withMaxResponseSize(responseValueLimit)
                        .build()
                )
                .build()

            val response = client.newCall(request).execute()

            DebugLogger.info(
                tag,
                "Url: $url: response code=${response.code}, isCompleted=${response.isCompleted}"
            )

            response.isCompleted && (response.code == HttpsURLConnection.HTTP_OK || response.code in 400..499)
        } catch (e: Exception) {
            DebugLogger.error(tag, e, "Error sending request with url = $url")
            false
        }
    }
}
