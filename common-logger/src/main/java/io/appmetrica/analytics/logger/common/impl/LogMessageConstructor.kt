package io.appmetrica.analytics.logger.common.impl

import android.util.Log
import java.util.Locale

internal class LogMessageConstructor @JvmOverloads constructor(
    private val maskApiKeysInLog: Boolean = false,
) {

    private val tag = "[LogMessageConstructor]"

    fun construct(tag: String, message: String?, vararg args: Any?): String {
        return tag + " " + prepareMessage(message, *args)
    }

    fun construct(tag: String, throwable: Throwable?, message: String?, vararg args: Any?): String {
        val nonNullMessage = (message.orEmpty()) + "\n" + Log.getStackTraceString(throwable)
        return construct(tag, nonNullMessage, *args)
    }

    private fun prepareMessage(message: String?, vararg args: Any?): String {
        var resultString = when {
            message == null -> ""
            args.isEmpty() -> message
            else -> try {
                String.format(Locale.US, message, *args)
            } catch (e: Throwable) {
                val errorMessage = "Attention!!! Invalid log format. See exception details above. Message: " +
                    "$message; arguments: ${args.contentToString()}"
                val maskedErrorMessage =
                    if (maskApiKeysInLog) ApiKeyLogSanitizer.maskApiKeysInLog(errorMessage) else errorMessage
                Log.e(this.tag, maskedErrorMessage, e)
                maskedErrorMessage
            }
        }
        if (maskApiKeysInLog) {
            resultString = ApiKeyLogSanitizer.maskApiKeysInLog(resultString)
        }
        return String.format(
            Locale.US,
            "[%d-%s] %s",
            currentThreadIdForLog(),
            Thread.currentThread().name,
            resultString
        )
    }

    @Suppress("DEPRECATION")
    private fun currentThreadIdForLog(): Long = Thread.currentThread().id
}
