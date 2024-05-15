package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.logger.internal.DebugLogger
import javax.net.ssl.HttpsURLConnection

private const val TAG = "[DefaultResponseValidityChecker]"

class DefaultResponseValidityChecker : ResponseValidityChecker {

    override fun isResponseValid(responseCode: Int): Boolean {
        val validResponse = responseCode != HttpsURLConnection.HTTP_BAD_REQUEST &&
            responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR
        DebugLogger.info(TAG, "isResponseValid for code = $responseCode is $validResponse")
        return validResponse
    }
}
