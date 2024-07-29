package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.io.FileUtils.copyToNullable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class FileVitalDataSource(
    private val context: Context,
    private val fileName: String
) : VitalDataSource {

    private val tag = "[FileVitalDataSource-$fileName]"

    override fun getVitalData(): String? = try {
        val file = FileUtils.getFileFromSdkStorage(context, fileName)
        file?.let {
            if (!file.exists()) {
                DebugLogger.info(
                    tag,
                    "Vital data for path = `${file.path}` does not exist. Try to import from old location"
                )
            }
            FileUtils.getFileFromAppStorage(context, fileName)?.copyToNullable(file)
            DebugLogger.info(tag, "Read data from file with name = ${file.path}")
            file.readText()
        }
    } catch (ex: Throwable) {
        DebugLogger.error(tag, ex, "File $fileName exception")
        null
    }

    override fun putVitalData(data: String) {
        DebugLogger.info(tag, "Write data to file with name = $fileName. Data = $data")
        try {
            FileUtils.getFileFromSdkStorage(context, fileName)?.writeText(data)
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "File $fileName exception")
        }
    }
}
