package io.appmetrica.analytics.coreutils.internal.io

import android.database.Cursor
import java.io.Closeable

object CloseableUtils {

    @JvmStatic
    fun Closeable?.closeSafely() {
        try {
            this?.close()
        } catch (ignored: Throwable) {
        }
    }

    @JvmStatic
    fun Cursor?.closeSafely() {
        try {
            this?.close()
        } catch (ignored: Throwable) {
        }
    }
}
