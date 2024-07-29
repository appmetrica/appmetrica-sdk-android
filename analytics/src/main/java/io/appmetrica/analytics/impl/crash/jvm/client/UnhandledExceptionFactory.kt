package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.plugins.StackTraceItem

internal object UnhandledExceptionFactory {

    @JvmStatic
    fun getUnhandledExceptionFromJava(
        exception: Throwable?,
        allThreads: AllThreads?,
        methodCallStacktrace: List<StackTraceElement>?,
        buildId: String?,
        isOffline: Boolean?
    ): UnhandledException = UnhandledException(
        exception?.let(ThrowableModelFactory::createModel),
        allThreads,
        methodCallStacktrace?.map(::StackTraceItemInternal),
        null,
        null,
        null,
        buildId,
        isOffline
    )

    @JvmStatic
    fun getUnhandledExceptionFromPlugin(
        exceptionClass: String?,
        exceptionMessage: String?,
        methodCallStacktrace: List<StackTraceItem>?,
        platform: String?,
        virtualMachineVersion: String?,
        environment: Map<String, String>?,
        buildId: String?,
        isOffline: Boolean?
    ): UnhandledException = UnhandledException(
        ThrowableModel(
            exceptionClass,
            exceptionMessage,
            methodCallStacktrace?.map(::StackTraceItemInternal),
            null,
            null
        ),
        null,
        null,
        platform,
        virtualMachineVersion,
        environment,
        buildId,
        isOffline
    )
}
