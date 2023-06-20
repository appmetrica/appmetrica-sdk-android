package io.appmetrica.analytics.impl.crash.client

internal class ThrowableModel(
    val exceptionClass: String?,
    val message: String?,
    val stacktrace: List<StackTraceItemInternal>?,
    val cause: ThrowableModel?,
    val suppressed: List<ThrowableModel>?
)
