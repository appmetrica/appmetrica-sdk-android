package io.appmetrica.analytics.network.impl

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.network.impl.utils.Utils
import io.appmetrica.analytics.network.internal.Call
import io.appmetrica.analytics.network.internal.NetworkClient
import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.network.internal.Response
import javax.net.ssl.HttpsURLConnection

internal class CallImpl @VisibleForTesting constructor(
    private val client: NetworkClient,
    private val request: Request,
    private val urlProvider: UrlProvider
) : Call {

    private val tag = "[NetworkCallImpl]"

    constructor(client: NetworkClient, request: Request) : this(client, request, UrlProvider())

    override fun execute(): Response {
        YLogger.info(tag, "Try to execute request [%s] for client [%s]", request, client)
        val openConnection = try {
            urlProvider.createUrl(request.url).openConnection()
        } catch (ex: Throwable) {
            YLogger.error(tag, ex)
            return Response(ex)
        }
        val httpsConnection = openConnection as? HttpsURLConnection?
            ?: with("Connection created for ${request.url} does not represent https connection") {
                YLogger.info(tag, this)
                return Response(IllegalArgumentException(this))
            }
        var completed: Boolean
        var exception: Throwable? = null
        var responseCode = 0
        var responseHeaders: Map<String, List<String>>? = null
        var responseData = ByteArray(0)
        var errorData = ByteArray(0)
        try {
            setUpBaseConnection(httpsConnection)
            if (request.method == "POST") {
                setUpPostConnection(httpsConnection)
            }
            responseCode = httpsConnection.responseCode
            responseHeaders = httpsConnection.headerFields
            responseData = Utils.readSafely(client.maxResponseSize) { httpsConnection.inputStream }
            errorData = Utils.readSafely(client.maxResponseSize) { httpsConnection.errorStream }
            completed = true
        } catch (ex: Throwable) {
            YLogger.error(tag, ex)
            exception = ex
            completed = false
        } finally {
            try {
                httpsConnection.disconnect()
            } catch (ex: Throwable) {
                YLogger.error(tag, ex)
            }
        }

        return Response(completed, responseCode, responseData, errorData, responseHeaders, exception).also {
            YLogger.info(tag, "Response: $it")
        }
    }

    private fun setUpBaseConnection(connection: HttpsURLConnection) {
        request.headers.entries.forEach { connection.addRequestProperty(it.key, it.value) }
        client.readTimeout?.let { connection.readTimeout = it }
        client.connectTimeout?.let { connection.connectTimeout = it }
        client.useCaches?.let { connection.useCaches = it }
        client.instanceFollowRedirects?.let { connection.instanceFollowRedirects = it }
        connection.requestMethod = request.method
        client.sslSocketFactory?.let { connection.sslSocketFactory = it }
    }

    private fun setUpPostConnection(connection: HttpsURLConnection) {
        connection.doOutput = true
        connection.outputStream?.use {
            it.write(request.body)
            it.flush()
        }
    }
}
