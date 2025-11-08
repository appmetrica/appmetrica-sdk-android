package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.system.SystemPropertiesHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

class OuterStoragePathProvider {

    private val tag = "[OverwrittenDbStoragePathProvider]"

    private val databasesDirSystemProperty = "ro.yndx.metrica.db.dir"
    private val databasesDirDebugProperty = "debug.yndx.iaa.db.dir"

    fun getPath(context: Context): File? {
        val systemRootDir = SystemPropertiesHelper.readSystemProperty(databasesDirSystemProperty)
        val debugRootDir = SystemPropertiesHelper.readSystemProperty(databasesDirDebugProperty)
        val dbRootDir = systemRootDir.ifBlank { debugRootDir }
        DebugLogger.info(tag, "SystemRootDir: $systemRootDir, debugRootDir: $debugRootDir, dbRootDir: $dbRootDir")
        if (dbRootDir.isBlank()) {
            return null
        }
        val appDbDir = File(dbRootDir, context.packageName)
        DebugLogger.info(tag, "AppDbDir: $appDbDir")
        return try {
            appDbDir.mkdirs()
            appDbDir
        } catch (ex: Exception) {
            DebugLogger.error(tag, "Cannot create db root directory %s", appDbDir.absolutePath)
            null
        }
    }
}
