package io.appmetrica.analytics.impl.component.session

internal class SessionArguments @JvmOverloads constructor(
    val creationElapsedRealtime: Long,
    val creationTimestamp: Long,
    val sessionRequestParams: SessionRequestParams? = null
) {
    override fun toString(): String {
        return "SessionArguments(" +
            "creationElapsedRealtime=$creationElapsedRealtime, " +
            "creationTimestamp=$creationTimestamp, " +
            "sessionRequestParams=$sessionRequestParams" +
            ")"
    }
}
