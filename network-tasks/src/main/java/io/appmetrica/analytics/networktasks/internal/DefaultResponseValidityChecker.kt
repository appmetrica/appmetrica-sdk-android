package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import javax.net.ssl.HttpsURLConnection

class DefaultResponseValidityChecker : ResponseValidityChecker {

    private val tag = "[DefaultResponseValidityChecker]"

    override fun isResponseValid(responseCode: Int): Boolean {
        val validResponse = responseCode != HttpsURLConnection.HTTP_BAD_REQUEST &&
            responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR
        DebugLogger.info(tag, "isResponseValid for code = $responseCode is $validResponse")
        return validResponse
    }
}
