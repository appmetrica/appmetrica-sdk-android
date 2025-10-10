package io.appmetrica.analytics.idsync.impl

internal class RequestResult(
    val type: String,
    val isCompleted: Boolean,
    val url: String?,
    val responseCodeIsValid: Boolean,
    val responseCode: Int,
    val responseBody: ByteArray,
    val responseHeaders: Map<String, List<String>>
) {

    override fun toString(): String {
        return "RequestResult(" +
            "type='$type', " +
            "isCompleted=$isCompleted, " +
            "url=$url, " +
            "responseCodeIsValid=$responseCodeIsValid, " +
            "responseCode=$responseCode, " +
            "responseBody=$responseBody, " +
            "responseHeaders=$responseHeaders" +
            ")"
    }
}
