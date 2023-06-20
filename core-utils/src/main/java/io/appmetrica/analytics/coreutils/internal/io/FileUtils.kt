package io.appmetrica.analytics.coreutils.internal.io

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import java.io.File

object FileUtils {

    private const val TAG = "[FileUtils]"
    const val SDK_STORAGE_RELATIVE_PATH = "/appmetrica/analytics"
    const val SDK_FILES_PREFIX = "appmetrica_analytics"

    @Volatile
    private var sdkStorage: File? = null

    @JvmStatic
    fun sdkStorage(context: Context): File? {
        if (sdkStorage == null) {
            synchronized(this) {
                if (sdkStorage == null) {
                    YLogger.info(TAG, "sdkStorage is null. So create it")
                }
                sdkStorage = createSdkStorage(getAppStorageDirectory(context))
            }
        }
        return sdkStorage
    }

    private fun createSdkStorage(file: File?): File? {
        if (file == null) {
            return null
        }
        val sdkStorage = File(file, SDK_STORAGE_RELATIVE_PATH)
        YLogger.info(TAG, "Sdk storage path is ${sdkStorage.path}")
        if (!sdkStorage.exists()) {
            YLogger.info(TAG, "Sdk storage is not exist. So create it.")
            val status = sdkStorage.mkdirs()
            YLogger.info(TAG, "Sdk storage creating status = $status")
        } else {
            YLogger.info(TAG, "Sdk storage exists.")
        }
        return sdkStorage
    }

    @JvmStatic
    fun getAppStorageDirectory(context: Context): File? = when {
        needToUseNoBackup() -> AppStorageDirectoryProviderForLollipop.getAppStorageDirectory(context)
        else -> context.filesDir
    }

    @JvmStatic
    fun getFileFromSdkStorage(context: Context, fileName: String): File? =
        sdkStorage(context)?.let { File(it, fileName) }

    @JvmStatic
    fun getFileFromAppStorage(context: Context, fileName: String): File? =
        getAppStorageDirectory(context)?.let { File(it, fileName) }

    @JvmStatic
    fun getFileFromPath(filePath: String): File = File(filePath)

    @JvmStatic
    fun needToUseNoBackup(): Boolean = AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)

    @JvmStatic
    @SuppressLint("NewApi")
    fun getAppDataDir(context: Context): File? {
        return if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
            AppDataDirProviderForN.dataDir(context)
        } else {
            context.filesDir?.parentFile
        }
    }

    @JvmStatic
    @VisibleForTesting
    fun resetSdkStorage() {
        synchronized(this) {
            sdkStorage = null
        }
    }

    @JvmStatic
    fun File?.move(to: File?): Boolean = this.moveByRename(to) || this.moveByCopy(to)

    fun File?.moveByCopy(to: File?): Boolean {
        if (this == null || to == null) {
            YLogger.info(TAG, "Source or destination is null, so ignore move")
            return false
        }
        if (this.exists()) {
            YLogger.info(TAG, "Move from `${this.path}` -> ${this.path} via copy")
            try {
                this.copyTo(to)
                this.delete()
                return true
            } catch (e: Throwable) {
                YLogger.error(TAG, e)
            }
        } else {
            YLogger.warning(TAG, "Source file with path `${this.path}` does not exist. Abort moving")
        }
        return false
    }

    fun File?.moveByRename(to: File?): Boolean = to?.let { this?.renameTo(to) } ?: false
}
