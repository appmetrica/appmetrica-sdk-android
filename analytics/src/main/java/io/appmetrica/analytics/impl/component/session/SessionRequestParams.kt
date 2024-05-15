package io.appmetrica.analytics.impl.component.session

import android.text.TextUtils
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.internal.DebugLogger
import org.json.JSONObject

internal class SessionRequestParams(requestParameters: JSONObject) {
    private val tag = "[SessionRequestParams]"
    private val analyticsSdkVersionName: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME,
        ""
    )
    private val analyticsSdkBuildNumber: String = requestParameters.optString(
        Constants.RequestParametersJsonKeys.ANALYTICS_SDK_BUILD_NUMBER,
        ""
    )
    private val appVersion: String = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_VERSION, "")
    private val appBuild: String = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_BUILD, "")
    private val osVersion: String = requestParameters.optString(Constants.RequestParametersJsonKeys.OS_VERSION, "")
    private val apiLevel: Int = requestParameters.optInt(Constants.RequestParametersJsonKeys.OS_API_LEVEL, -1)
    private val attributionId: Int = requestParameters.optInt(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, 0)

    fun areParamsSameAsInConfig(reportRequestConfig: ReportRequestConfig): Boolean {
        val paramsAreSame = listOf(
            TextUtils.equals(reportRequestConfig.analyticsSdkVersionName, analyticsSdkVersionName),
            TextUtils.equals(reportRequestConfig.analyticsSdkBuildNumber, analyticsSdkBuildNumber),
            TextUtils.equals(reportRequestConfig.appVersion, appVersion),
            TextUtils.equals(reportRequestConfig.appBuildNumber, appBuild),
            TextUtils.equals(reportRequestConfig.osVersion, osVersion),
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
        return "SessionRequestParams(kitVersionName='$analyticsSdkVersionName', " +
            "kitBuildNumber='$analyticsSdkBuildNumber', " +
            "appVersion='$appVersion', appBuild='$appBuild', osVersion='$osVersion', apiLevel=$apiLevel, " +
            "attributionId=$attributionId)"
    }
}
