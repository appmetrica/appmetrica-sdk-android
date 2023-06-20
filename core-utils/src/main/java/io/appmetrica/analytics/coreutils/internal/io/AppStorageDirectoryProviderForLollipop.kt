package io.appmetrica.analytics.coreutils.internal.io

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import java.io.File

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@DoNotInline
object AppStorageDirectoryProviderForLollipop {
    fun getAppStorageDirectory(context: Context): File? = context.noBackupFilesDir
}
