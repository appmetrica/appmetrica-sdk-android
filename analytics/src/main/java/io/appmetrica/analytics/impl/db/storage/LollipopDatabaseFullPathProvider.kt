package io.appmetrica.analytics.impl.db.storage

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import java.io.File

@DoNotInline
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class LollipopDatabaseFullPathProvider(
    private val relativePathFormer: RelativePathFormer
) : DatabaseFullPathProvider {

    override fun fullPath(context: Context, simpleName: String): File =
        File(context.noBackupFilesDir, relativePathFormer.preparePath(simpleName))
}
