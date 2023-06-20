package io.appmetrica.analytics.impl.crash.client

import android.annotation.SuppressLint
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.impl.Utils

internal object ThrowableModelFactory {

    private const val maxCauseDepth = 30
    private const val maxSuppressedDepth = 1

    @JvmStatic
    fun createModel(throwable: Throwable): ThrowableModel {
        return createModel(throwable, 1, 0)
    }

    @SuppressLint("NewApi")
    private fun createModel(throwable: Throwable, maxDepth: Int, curDepth: Int): ThrowableModel {
        return ThrowableModel(
            throwable.javaClass.name,
            throwable.message,
            Utils.getStackTraceSafely(throwable).map(::StackTraceItemInternal),
            throwable.cause?.takeIf { curDepth < maxDepth }?.let { createModel(it, maxCauseDepth, curDepth + 1) },
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT) && curDepth < maxDepth) {
                throwable.suppressed.map { createModel(it, maxSuppressedDepth, curDepth) }
            } else {
                null
            }
        )
    }
}
