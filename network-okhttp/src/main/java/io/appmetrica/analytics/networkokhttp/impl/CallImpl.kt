package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.coreutils.internal.io.InputStreamUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.networkapi.Response
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

internal class CallImpl(
    private val okHttpClient: OkHttpClient,
    private val request: Request,
    private val settings: NetworkClientSettings,
) : Call() {

    private val tag = "[CallImpl]"

    override fun execute(): Response {
        DebugLogger.info(tag, "Try to execute request [$request] by OkHttp client")

        val collectMetrics = settings.collectMetrics == true
        val metricsListener = if (collectMetrics) NetworkCallMetricsListener() else null
        val client = if (metricsListener != null) {
            okHttpClient.newBuilder()
                .eventListenerFactory { metricsListener }
                .build()
        } else {
            okHttpClient
        }

        try {
            val response = client.newCall(request.asOkHttpRequest()).execute()
            val responseData = InputStreamUtils.readSafelyApprox(settings.maxResponseSize) {
                response.body?.byteStream()
            }

            return Response.Builder(
                isCompleted = true,
                code = response.code,
                responseData = responseData
            )
                .withHeaders(response.headers.toMap())
                .withUrl(response.request.url.toString())
                .withMetrics(metricsListener?.buildMetrics())
                .build()
                .also {
                    DebugLogger.info(tag, "Response $it")
                }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            return Response.Builder(ex)
                .withMetrics(metricsListener?.buildMetrics())
                .build()
        }
    }

    private fun Headers.toMap(): Map<String, List<String>> =
        (0 until size).groupBy({ name(it) }, { value(it) })

    private fun Request.asOkHttpRequest(): okhttp3.Request = okhttp3.Request.Builder()
        .url(url)
        .apply {
            when (method) {
                Request.Method.GET -> get()
                Request.Method.HEAD -> head()
                Request.Method.POST,
                Request.Method.PUT,
                Request.Method.PATCH,
                Request.Method.DELETE,
                Request.Method.OPTIONS -> method(method.methodName, body.toRequestBody())
            }
            headers.forEach {
                addHeader(it.key, it.value)
            }
        }
        .build()
}
