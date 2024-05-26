package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import java.io.File

@DoNotInline
internal class DatabaseFullPathProviderImpl(
    private val relativePathFormer: RelativePathFormer
) : DatabaseFullPathProvider {

    override fun fullPath(context: Context, simpleName: String): File =
        File(context.noBackupFilesDir, relativePathFormer.preparePath(simpleName))
}
