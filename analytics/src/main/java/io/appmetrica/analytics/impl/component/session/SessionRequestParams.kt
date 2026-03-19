package io.appmetrica.analytics.impl.component.session

import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class SessionRequestParams(requestParameters: JSONObject) {
    private val tag = "[SessionRequestParams]"
    private val requestParametersString: String = requestParameters.toString()
    private val deviceId: String = requestParameters.optString(Constants.RequestParametersJsonKeys.DEVICE_ID, "")
    private val uuid: String = requestParameters.optString(Constants.RequestParametersJsonKeys.UUID, "")
    private val appVersion: String = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_VERSION, "")
    private val appBuild: String = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_BUILD, "")
    private val analyticsSdkBuildType: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ANALYTICS_SDK_BUILD_TYPE,
        ""
    )
    private val osVersion: String = requestParameters.optString(Constants.RequestParametersJsonKeys.OS_VERSION, "")
    private val apiLevel: Int = requestParameters.optInt(Constants.RequestParametersJsonKeys.OS_API_LEVEL, -1)
    private val locale: String = requestParameters.optString(Constants.RequestParametersJsonKeys.LOCALE, "")
    private val deviceRootStatus: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ROOT_STATUS,
        ""
    )
    private val appDebuggable: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.APP_DEBUGGABLE,
        ""
    )
    private val appFramework: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.APP_FRAMEWORK,
        ""
    )
    private val attributionId: Int = requestParameters.optInt(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, 0)
    private val analyticsSdkVersionName: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME,
        ""
    )
    private val analyticsSdkBuildNumber: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ANALYTICS_SDK_BUILD_NUMBER,
        ""
    )

    fun toRequestParametersString(): String = requestParametersString

    fun areParamsSameAsInConfig(reportRequestConfig: ReportRequestConfig): Boolean {
        val paramsAreSame = listOf(
            reportRequestConfig.analyticsSdkVersionName == analyticsSdkVersionName,
            reportRequestConfig.analyticsSdkBuildNumber == analyticsSdkBuildNumber,
            reportRequestConfig.appVersion == appVersion,
            reportRequestConfig.appBuildNumber == appBuild,
            reportRequestConfig.osVersion == osVersion,
            apiLevel == reportRequestConfig.osApiLevel,
            attributionId == reportRequestConfig.attributionId
        ).all { it }

        if (!paramsAreSame) {
            DebugLogger.info(
                tag,
                "SessionRequestParameters are not equal: %s and %s",
                this,
                requestConfigToString(reportRequestConfig)
            )
        }
        return paramsAreSame
    }

    private fun requestConfigToString(reportRequestConfig: ReportRequestConfig): String {
        return "ReportRequestConfig{" +
            "kitVersionName='" + reportRequestConfig.analyticsSdkVersionName + '\'' +
            ", kitBuildNumber='" + reportRequestConfig.analyticsSdkBuildNumber + '\'' +
            ", appVersion='" + reportRequestConfig.appVersion + '\'' +
            ", appBuild='" + reportRequestConfig.appBuildNumber + '\'' +
            ", osVersion='" + reportRequestConfig.osVersion + '\'' +
            ", apiLevel=" + reportRequestConfig.osApiLevel +
            '}'
    }

    override fun toString(): String {
        return "SessionRequestParams(" +
            "deviceId='$deviceId', " +
            "uuid='$uuid', " +
            "appVersion='$appVersion', " +
            "appBuild='$appBuild', " +
            "kitBuildType='$analyticsSdkBuildType', " +
            "osVersion='$osVersion', " +
            "apiLevel=$apiLevel, " +
            "locale='$locale', " +
            "deviceRootStatus='$deviceRootStatus', " +
            "appDebuggable='$appDebuggable', " +
            "appFramework='$appFramework', " +
            "attributionId=$attributionId, " +
            "kitVersionName='$analyticsSdkVersionName', " +
            "kitBuildNumber='$analyticsSdkBuildNumber')"
    }
}
