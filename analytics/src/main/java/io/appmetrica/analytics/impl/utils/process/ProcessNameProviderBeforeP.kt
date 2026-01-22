package io.appmetrica.analytics.impl.utils.process

import android.annotation.SuppressLint
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline

@DoNotInline
internal class ProcessNameProviderBeforeP : ProcessNameProvider {

    @Volatile
    private var mProcessName: String? = null

    /**
     * Retrieves the name of the current process.
     *
     * @return The name of the current process. E.g. "org.chromium.chrome:privileged_process0".
     */
    override fun getProcessName(): String? {
        // Once we drop support JB, this method can be simplified to not cache sProcessName and call
        // ActivityThread.currentProcessName().
        if (mProcessName != null) {
            return mProcessName
        }

        // Before JB MR2, currentActivityThread() returns null when called on a non-UI thread.
        // Cache the name to allow other threads to access it.
        synchronized(this) {
            if (mProcessName == null) {
                mProcessName = extractProcessFromActivityThread()
            }
        }

        return mProcessName
    }

    @SuppressLint("PrivateApi")
    private fun extractProcessFromActivityThread(): String {
        try {
            // An even more convenient ActivityThread.currentProcessName() exists, but was not added
            // until JB MR2.
            val activityThreadClazz = Class.forName("android.app.ActivityThread")
            val activityThread =
                activityThreadClazz.getMethod("currentActivityThread").invoke(null)
            // Before JB MR2, currentActivityThread() returns null when called on a non-UI thread.
            // Cache the name to allow other threads to access it.
            return activityThreadClazz.getMethod("getProcessName").invoke(activityThread) as String
        } catch (e: Throwable) { // No multi-catch below API level 19 for reflection exceptions.
            // If fallback logic is ever needed, refer to:
            // https://chromium-review.googlesource.com/c/chromium/src/+/905563/1
            throw RuntimeException(e)
        }
    }
}
