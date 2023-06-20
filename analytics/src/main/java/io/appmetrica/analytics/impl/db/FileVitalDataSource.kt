package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.io.FileUtils.move
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import java.io.FileNotFoundException

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
            FileUtils.getFileFromAppStorage(context, fileName)?.move(file)
            YLogger.info(TAG, "Read data from file with name = ${file.path}")
            file.readText()
        }
    } catch (ex: FileNotFoundException) {
        AppMetricaSelfReportFacade.getReporter().reportEvent(
            "vital_data_provider_read_file_not_found",
            mapOf("fileName" to fileName)
        )
        YLogger.e(ex, "File $fileName exception")
        null
    } catch (ex: Throwable) {
        AppMetricaSelfReportFacade.getReporter()
            .reportEvent(
                "vital_data_provider_read_exception",
                mapOf(
                    "fileName" to fileName,
                    "exception" to ex::class.simpleName
                )
            )
        AppMetricaSelfReportFacade.getReporter().reportError(
            "Error during reading file with name $fileName",
            ex
        )
        YLogger.e(ex, "File $fileName exception")
        null
    }

    override fun putVitalData(data: String) {
        YLogger.info(TAG, "Write data to file with name = $fileName. Data = $data")
        try {
            FileUtils.getFileFromSdkStorage(context, fileName)?.writeText(data)
        } catch (ex: FileNotFoundException) {
            AppMetricaSelfReportFacade.getReporter().reportEvent(
                "vital_data_provider_write_file_not_found",
                mapOf("fileName" to fileName)
            )
            YLogger.e(ex, "File $fileName exception")
        } catch (ex: Throwable) {
            AppMetricaSelfReportFacade.getReporter()
                .reportEvent(
                    "vital_data_provider_write_exception",
                    mapOf(
                        "fileName" to fileName,
                        "exception" to ex::class.simpleName
                    )
                )
            AppMetricaSelfReportFacade.getReporter().reportError(
                "Error during writing file with name $fileName",
                ex
            )
            YLogger.e(ex, "File $fileName exception")
        }
    }
}
