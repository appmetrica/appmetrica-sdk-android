package io.appmetrica.analytics.networklegacy.impl

import io.appmetrica.analytics.coreutils.internal.io.InputStreamUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.networkapi.Response
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class CallImpl(
    private val settings: NetworkClientSettings,
    private val request: Request,
) : Call() {

    private val tag = "[CallImpl]"

    override fun execute(): Response {
        DebugLogger.info(tag, "Try to execute request [$request] by Legacy client")

        val httpsConnection = try {
            URL(request.url).openConnection() as? HttpsURLConnection?
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            return Response.Builder(ex).build()
        }

        if (httpsConnection == null) {
            val errorMessage = "Connection created for ${request.url} does not represent https connection"
            DebugLogger.error(tag, errorMessage)
            return Response.Builder(IllegalArgumentException(errorMessage)).build()
        }

        try {
            setUpConnection(httpsConnection)

            val responseData = InputStreamUtils.readSafelyApprox(settings.maxResponseSize) {
                if (httpsConnection.responseCode >= 400) {
                    httpsConnection.errorStream
                } else {
                    httpsConnection.inputStream
                }
            }

            return Response.Builder(
                isCompleted = true,
                code = httpsConnection.responseCode,
                responseData = responseData
            ).apply {
                httpsConnection.headerFields?.let { withHeaders(it.filterNulls()) }
                httpsConnection.url?.toString()?.let { withUrl(it) }
            }.build().also {
                DebugLogger.info(tag, "Response: $it")
            }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            return Response.Builder(ex).build()
        } finally {
            try {
                httpsConnection.disconnect()
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex)
            }
        }
    }

    private fun setUpConnection(connection: HttpsURLConnection) {
        request.headers.entries.forEach { entry ->
            connection.addRequestProperty(entry.key, entry.value)
        }
        settings.readTimeout?.let { connection.readTimeout = it }
        settings.connectTimeout?.let { connection.connectTimeout = it }
        settings.useCaches?.let { connection.useCaches = it }
        settings.instanceFollowRedirects?.let { connection.instanceFollowRedirects = it }
        settings.sslSocketFactory?.let { connection.sslSocketFactory = it }
        connection.requestMethod = request.method.methodName

        if (request.method == Request.Method.POST) {
            connection.doOutput = true
            connection.outputStream?.use {
                it.write(request.body)
                it.flush()
            }
        }
    }

    private fun Map<String?, List<String?>?>.filterNulls(): Map<String, List<String>> {
        return mapNotNull { (key, value) ->
            if (key == null) {
                return@mapNotNull null
            }
            if (value == null) {
                return@mapNotNull key to emptyList()
            }
            return@mapNotNull key to value.filterNotNull()
        }.toMap()
    }
}
