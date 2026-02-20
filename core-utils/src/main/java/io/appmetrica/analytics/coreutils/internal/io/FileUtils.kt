package io.appmetrica.analytics.coreutils.internal.io

import android.content.Context
import android.os.Build
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

object FileUtils {

    private const val TAG = "[FileUtils]"
    const val SDK_STORAGE_RELATIVE_PATH = "/appmetrica/analytics"
    const val SDK_FILES_PREFIX = "appmetrica_analytics"
    private const val NATIVE_CRASH_FOLDER_NAME = "native_crashes"
    private const val CRASHES_DIR_NAME = "crashes"

    @Volatile
    private var sdkStorage: File? = null

    @JvmStatic
    fun sdkStorage(context: Context): File? {
        if (sdkStorage == null) {
            synchronized(this) {
                if (sdkStorage == null) {
                    DebugLogger.info(TAG, "sdkStorage is null. So create it")
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
        DebugLogger.info(TAG, "Sdk storage path is ${sdkStorage.path}")
        if (!sdkStorage.exists()) {
            DebugLogger.info(TAG, "Sdk storage is not exist. So create it.")
            val status = sdkStorage.mkdirs()
            DebugLogger.info(TAG, "Sdk storage creating status = $status")
        } else {
            DebugLogger.info(TAG, "Sdk storage exists.")
        }
        return sdkStorage
    }

    @JvmStatic
    fun getAppStorageDirectory(context: Context): File? = context.noBackupFilesDir

    @JvmStatic
    fun getFileFromSdkStorage(context: Context, fileName: String): File? =
        sdkStorage(context)?.let { File(it, fileName) }

    @JvmStatic
    fun getFileFromAppStorage(context: Context, fileName: String): File? =
        getAppStorageDirectory(context)?.let { File(it, fileName) }

    @JvmStatic
    fun getCrashesDirectory(context: Context): File? =
        getFileFromSdkStorage(context, CRASHES_DIR_NAME)

    @JvmStatic // for tests
    fun getNativeCrashDirectory(context: Context): File? =
        getFileFromSdkStorage(context, NATIVE_CRASH_FOLDER_NAME)

    @JvmStatic
    fun getFileFromPath(filePath: String): File = File(filePath)

    @JvmStatic
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

    @JvmStatic
    fun File?.copyToNullable(to: File?): Boolean {
        if (this == null || to == null) {
            DebugLogger.info(TAG, "Source or destination is null, so ignore copying")
            return false
        }
        if (this.exists()) {
            DebugLogger.info(TAG, "Copy from `${this.path}` -> ${this.path} via copy")
            try {
                this.copyTo(to)
                return true
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e)
            }
        } else {
            DebugLogger.warning(TAG, "Source file with path `${this.path}` does not exist. Abort moving")
        }
        return false
    }

    fun File?.moveByCopy(to: File?): Boolean {
        if (this == null || to == null) {
            DebugLogger.info(TAG, "Source or destination is null, so ignore move")
            return false
        }
        if (this.exists()) {
            DebugLogger.info(TAG, "Move from `${this.path}` -> ${this.path} via copy")
            try {
                this.copyTo(to)
                this.delete()
                return true
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e)
            }
        } else {
            DebugLogger.warning(TAG, "Source file with path `${this.path}` does not exist. Abort moving")
        }
        return false
    }

    fun File?.moveByRename(to: File?): Boolean = to?.let { this?.renameTo(to) } ?: false
}
