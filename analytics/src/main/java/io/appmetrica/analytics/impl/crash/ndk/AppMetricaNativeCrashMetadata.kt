package io.appmetrica.analytics.impl.crash.ndk

import android.util.Base64
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.parsing.optStringOrNull
import io.appmetrica.analytics.impl.CounterConfigurationReporterType
import org.json.JSONObject

data class AppMetricaNativeCrashMetadata(
    val apiKey: String,
    val packageName: String,
    val reporterType: CounterConfigurationReporterType,
    val processID: Int,
    val processSessionID: String,
    val errorEnvironment: String?,
)

class AppMetricaNativeCrashMetadataSerializer {
    companion object {
        private const val TAG = "[AppMetricaNativeCrashMetadataSerializer]"

        private const val API_KEY_KEY = "apiKey"
        private const val PACKAGE_NAME_KEY = "packageName"
        private const val REPORTER_TYPE_KEY = "reporterType"
        private const val PROCESS_ID_KEY = "processID"
        private const val PROCESS_SESSION_ID_KEY = "processSessionID"
        private const val ERROR_ENVIRONMENT_KEY = "errorEnvironment"
    }

    fun serialize(metadata: AppMetricaNativeCrashMetadata): String = try {
        val json = JSONObject()
            .put(API_KEY_KEY, metadata.apiKey)
            .put(PACKAGE_NAME_KEY, metadata.packageName)
            .put(REPORTER_TYPE_KEY, metadata.reporterType.stringValue)
            .put(PROCESS_ID_KEY, metadata.processID)
            .put(PROCESS_SESSION_ID_KEY, metadata.processSessionID)
            .put(ERROR_ENVIRONMENT_KEY, metadata.errorEnvironment)
        Base64.encodeToString(json.toString().toByteArray(), Base64.DEFAULT)
    } catch (t: Throwable) {
        YLogger.error(TAG, "Failed to serialize appmetrica native crash metadata", t)
        ""
    }

    fun deserialize(string: String): AppMetricaNativeCrashMetadata? = try {
        val json = JSONObject(String(Base64.decode(string, Base64.DEFAULT)))
        AppMetricaNativeCrashMetadata(
            apiKey = json.getString(API_KEY_KEY),
            packageName = json.getString(PACKAGE_NAME_KEY),
            reporterType = CounterConfigurationReporterType.fromStringValue(json.getString(REPORTER_TYPE_KEY)),
            processID = json.getInt(PROCESS_ID_KEY),
            processSessionID = json.getString(PROCESS_SESSION_ID_KEY),
            errorEnvironment = json.optStringOrNull(ERROR_ENVIRONMENT_KEY)
        )
    } catch (t: Throwable) {
        YLogger.error(TAG, "Failed to deserialize appmetrica native crash metadata: $string", t)
        null
    }
}
