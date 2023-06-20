package io.appmetrica.analytics.coreutils.internal.io

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import java.io.File

@TargetApi(Build.VERSION_CODES.N)
@DoNotInline
object AppDataDirProviderForN {

    fun dataDir(context: Context): File? = context.dataDir
}
