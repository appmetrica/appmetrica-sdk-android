package io.appmetrica.analytics.impl.db

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.logger.internal.DebugLogger
import org.json.JSONObject

internal class VitalDataProvider(
    private val primaryDataSource: VitalDataSource,
    private val backupDataSource: VitalDataSource,
    private val tag: String,
    private val vitalDataProviderStateMerger: VitalDataProviderStateMerger
) {
    private lateinit var fileContents: JSONObject

    @WorkerThread
    @Synchronized
    fun getOrLoadData(): JSONObject {
        if (!this::fileContents.isInitialized) {
            DebugLogger.info(tag, "Initial loading from storages for first getOrLoadData")
            val primaryJson = primaryDataSource.read()
            val backupJson = backupDataSource.read()
            val content = vitalDataProviderStateMerger.merge(primaryJson, backupJson)
            fileContents = content
            save(content)
        }
        return fileContents
    }

    private fun VitalDataSource.read(): JSONObject = try {
        getVitalData()?.let { JSONObject(it) } ?: JSONObject()
    } catch (ex: Throwable) {
        DebugLogger.error(tag, ex)
        JSONObject()
    }

    @WorkerThread
    @Synchronized
    fun save(contents: JSONObject) {
        DebugLogger.info(tag, "Save data = $contents")
        val data = contents.toString()
        primaryDataSource.saveSafely(data)
        backupDataSource.saveSafely(data)
    }

    private fun VitalDataSource.saveSafely(content: String) {
        try {
            putVitalData(content)
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
        }
    }
}
