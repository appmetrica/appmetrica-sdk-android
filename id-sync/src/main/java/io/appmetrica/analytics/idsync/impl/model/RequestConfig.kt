package io.appmetrica.analytics.idsync.impl.model

internal data class RequestConfig(
    val type: String,
    val url: String,
    val preconditions: Preconditions,
    val headers: Map<String, List<String>>,
    val resendIntervalForValidResponse: Long,
    val resendIntervalForInvalidResponse: Long,
    val validResponseCodes: List<Int>,
    val reportEventEnabled: Boolean,
    val reportUrl: String?
)
