package io.appmetrica.analytics.idsync.impl.model

internal enum class RequestAttemptResult(
    val value: String
) {
    NONE("none"),
    SUCCESS("success"),
    INCOMPATIBLE_PRECONDITION("incompatible_precondition"),
    FAILURE("failure")
    ;

    companion object {
        fun fromString(value: String): RequestAttemptResult {
            return values().firstOrNull { it.value == value } ?: NONE
        }
    }
}
