package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.io.FileUtils.copyToNullable
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[FileVitalDataSource]"

class FileVitalDataSource(
    private val context: Context,
    private val fileName: String
) : VitalDataSource {

    override fun getVitalData(): String? = try {
        val file = FileUtils.getFileFromSdkStorage(context, fileName)
        file?.let {
            if (!file.exists()) {
                YLogger.info(
                    TAG,
                    "Vital data for path = `${file.path}` does not exist. Try to import from old location"
                )
            }
            FileUtils.getFileFromAppStorage(context, fileName)?.copyToNullable(file)
            YLogger.info(TAG, "Read data from file with name = ${file.path}")
            file.readText()
        }
    } catch (ex: Throwable) {
        YLogger.e(ex, "File $fileName exception")
        null
    }

    override fun putVitalData(data: String) {
        YLogger.info(TAG, "Write data to file with name = $fileName. Data = $data")
        try {
            FileUtils.getFileFromSdkStorage(context, fileName)?.writeText(data)
        } catch (ex: Throwable) {
            YLogger.e(ex, "File $fileName exception")
        }
    }
}
