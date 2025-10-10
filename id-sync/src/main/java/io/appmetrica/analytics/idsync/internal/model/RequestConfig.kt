package io.appmetrica.analytics.idsync.internal.model

class RequestConfig(
    val type: String,
    val url: String,
    val preconditions: Preconditions,
    val headers: Map<String, List<String>>,
    val resendIntervalForValidResponse: Long,
    val resendIntervalForInvalidResponse: Long,
    val validResponseCodes: List<Int>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestConfig

        if (resendIntervalForValidResponse != other.resendIntervalForValidResponse) return false
        if (resendIntervalForInvalidResponse != other.resendIntervalForInvalidResponse) return false
        if (type != other.type) return false
        if (url != other.url) return false
        if (preconditions != other.preconditions) return false
        if (headers != other.headers) return false
        if (validResponseCodes != other.validResponseCodes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resendIntervalForValidResponse.hashCode()
        result = 31 * result + resendIntervalForInvalidResponse.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + preconditions.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + validResponseCodes.hashCode()
        return result
    }

    override fun toString(): String {
        return "RequestConfig(" +
            "type='$type', " +
            "url='$url', " +
            "preconditions=$preconditions, " +
            "headers=$headers, " +
            "resendIntervalForValidResponse=$resendIntervalForValidResponse, " +
            "resendIntervalForInvalidResponse=$resendIntervalForInvalidResponse, " +
            "validResponseCodes=$validResponseCodes" +
            ")"
    }
}
