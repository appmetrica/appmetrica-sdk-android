package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import java.io.File

internal interface DatabaseFullPathProvider {

    fun fullPath(context: Context, simpleName: String): File
}
