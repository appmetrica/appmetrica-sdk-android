package io.appmetrica.analytics.networktasks.impl

import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils
import io.appmetrica.analytics.logger.internal.DebugLogger
import io.appmetrica.analytics.network.internal.NetworkClient
import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import java.util.concurrent.TimeUnit

private const val TAG = "[NetworkTaskPerformingStrategy]"

internal class NetworkTaskPerformingStrategy {

    fun performRequest(task: NetworkTask): Boolean {
        if (task.onPerformRequest()) {
            val url = task.url
            DebugLogger.info(TAG, "Task %s perform request: %s", task.description(), url)
            if (url == null || TextUtils.isEmpty(url.trim())) {
                val errorMessage = "Task ${task.description()} url is `$url`. " +
                    "All hosts = ${task.underlyingTask.fullUrlFormer.allHosts?.toString()}"
                task.onRequestError(IllegalArgumentException(errorMessage))
                return false
            }
            val requestBuilder = Request.Builder(url)
                .addHeader(Constants.Headers.ACCEPT, Constants.Config.TYPE_JSON)
                .addHeader(
                    Constants.Headers.USER_AGENT,
                    task.userAgent
                )
            val requestDataHolder = task.requestDataHolder
            applyRequestHeaders(requestBuilder, requestDataHolder.headers)
            if (NetworkTask.Method.POST == requestDataHolder.method) {
                val postData = requestDataHolder.postData
                if (postData?.isNotEmpty() == true) {
                    requestBuilder.post(postData)
                    requestDataHolder.sendTimestamp?.let {
                        requestBuilder.addHeader(
                            Constants.Headers.SEND_TIMESTAMP,
                            TimeUnit.MILLISECONDS.toSeconds(it).toString()
                        )
                    }
                    requestDataHolder.sendTimezoneSec?.let {
                        requestBuilder.addHeader(Constants.Headers.SEND_TIMEZONE, it.toString())
                    }
                }
            }
            val client = NetworkClient.Builder()
                .withConnectTimeout(Constants.Config.REQUEST_TIMEOUT)
                .withReadTimeout(Constants.Config.REQUEST_TIMEOUT)
                .withSslSocketFactory(task.sslSocketFactory)
                .build()
            val response = client.newCall(requestBuilder.build()).execute()
            val responseCode = response.code
            val responseDataHolder = task.responseDataHolder
            responseDataHolder.responseCode = responseCode
            responseDataHolder.responseHeaders = CollectionUtils.convertMapKeysToLowerCase(response.headers)
            if (responseDataHolder.isValidResponse) {
                DebugLogger.info(
                    TAG,
                    "Task response: %d, desc: %s, url: %s, responseHeaders: %s",
                    responseCode,
                    task.description(),
                    url,
                    responseDataHolder.responseHeaders
                )
                responseDataHolder.responseData = response.responseData
            } else {
                DebugLogger.warning(
                    TAG,
                    "Task error response: %d, desc: %s; errorStream: %s; url = %s",
                    responseCode,
                    task.description(),
                    String(response.errorData),
                    url
                )
            }
            if (response.isCompleted) {
                return task.onRequestComplete()
            } else {
                task.onRequestError(response.exception)
            }
        } else {
            DebugLogger.info(TAG, "Will not perform task %s", task.description())
            task.onRequestError(null)
        }
        return false
    }

    private fun applyRequestHeaders(requestBuilder: Request.Builder, headers: Map<String, List<String>>) {
        headers.entries.forEach {
            requestBuilder.addHeader(it.key, it.value.joinToString(","))
        }
    }
}
