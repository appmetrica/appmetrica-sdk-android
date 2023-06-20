package io.appmetrica.analytics.networktasks.internal.utils

import javax.net.ssl.HttpsURLConnection

object Utils {

    @JvmStatic
    fun isBadRequest(code: Int): Boolean = code == HttpsURLConnection.HTTP_BAD_REQUEST
}
