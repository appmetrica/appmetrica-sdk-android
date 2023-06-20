package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import java.io.File

internal class OuterRootDatabaseFullPathProvider(
    private val root: File,
    private val relativePathFormer: RelativePathFormer
) : DatabaseFullPathProvider {

    override fun fullPath(context: Context, simpleName: String): File =
        File(root, relativePathFormer.preparePath(simpleName))
}
