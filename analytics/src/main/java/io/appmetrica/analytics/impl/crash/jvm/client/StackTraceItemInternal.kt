package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.plugins.StackTraceItem

internal class StackTraceItemInternal(
    val className: String?,
    val fileName: String?,
    val line: Int?,
    val column: Int?,
    val methodName: String?,
    val isAndroidNative: Boolean?
) {

    constructor(stackTraceItem: StackTraceItem) : this(
        stackTraceItem.className,
        stackTraceItem.fileName,
        stackTraceItem.line,
        stackTraceItem.column,
        stackTraceItem.methodName,
        null
    )

    constructor(stackTraceElement: StackTraceElement) : this(
        stackTraceElement.className,
        stackTraceElement.fileName,
        stackTraceElement.lineNumber,
        null,
        stackTraceElement.methodName,
        stackTraceElement.isNativeMethod
    )
}
