package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import java.io.File

internal class PreLollipopDatabaseFullPathProvider(
    private val relativePathFormer: RelativePathFormer
) : DatabaseFullPathProvider {

    override fun fullPath(context: Context, simpleName: String): File =
        context.getDatabasePath(relativePathFormer.preparePath(simpleName))
}
