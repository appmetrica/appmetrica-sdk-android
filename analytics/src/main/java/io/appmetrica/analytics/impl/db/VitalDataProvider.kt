package io.appmetrica.analytics.impl.db

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
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
            YLogger.info(tag, "Initial loading from storages for first getOrLoadData")
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
        processException(ex)
        JSONObject()
    }

    @WorkerThread
    @Synchronized
    fun save(contents: JSONObject) {
        YLogger.info(tag, "Save data = $contents")
        val data = contents.toString()
        primaryDataSource.saveSafely(data)
        backupDataSource.saveSafely(data)
    }

    private fun VitalDataSource.saveSafely(content: String) {
        try {
            putVitalData(content)
        } catch (ex: Throwable) {
            processException(ex)
        }
    }

    private fun processException(ex: Throwable) {
        AppMetricaSelfReportFacade.getReporter()
            .reportEvent(
                "vital_data_provider_exception",
                mapOf(
                    "tag" to tag,
                    "exception" to ex::class.simpleName
                )
            )
        AppMetricaSelfReportFacade.getReporter().reportError(
            "Error during reading vital data for tag = $tag",
            ex
        )
        YLogger.error(tag, ex)
    }
}
