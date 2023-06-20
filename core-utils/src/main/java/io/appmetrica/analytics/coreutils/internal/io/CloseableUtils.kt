package io.appmetrica.analytics.coreutils.internal.io

import android.database.Cursor
import java.io.Closeable

fun Closeable?.closeSafely() {
    try {
        this?.close()
    } catch (ignored: Throwable) {
    }
}

fun Cursor?.closeSafely() {
    try {
        this?.close()
    } catch (ignored: Throwable) {
    }
}
