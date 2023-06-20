package io.appmetrica.analytics.impl.db.session

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.ServerTime
import io.appmetrica.analytics.impl.utils.TimeUtils
import org.json.JSONObject

class DbSessionModelFactory(
    private val reportRequestConfig: ReportRequestConfig,
    private val id: Long?,
    private val type: SessionType?,
    private val startTime: Long?
) {
    private val tag = "[DbSessionModel]"

    fun create() = DbSessionModel(
        id,
        type,
        getSessionReportRequestParameters(),
        getSessionDescription(),
    )

    private fun getSessionDescription() = DbSessionModel.Description(
        startTime,
        TimeUtils.getServerTimeOffset(),
        ServerTime.getInstance().isUncheckedTime
    )

    private fun getSessionReportRequestParameters(): String {
        val requestParameters = try {
            JSONObject()
                .put(Constants.RequestParametersJsonKeys.DEVICE_ID, reportRequestConfig.deviceId)
                .put(Constants.RequestParametersJsonKeys.UUID, reportRequestConfig.uuid)
                .put(Constants.RequestParametersJsonKeys.APP_VERSION, reportRequestConfig.appVersion)
                .put(Constants.RequestParametersJsonKeys.APP_BUILD, reportRequestConfig.appBuildNumber)
                .put(Constants.RequestParametersJsonKeys.KIT_BUILD_TYPE, reportRequestConfig.kitBuildType)
                .put(Constants.RequestParametersJsonKeys.OS_VERSION, reportRequestConfig.osVersion)
                .put(Constants.RequestParametersJsonKeys.OS_API_LEVEL, reportRequestConfig.osApiLevel)
                .put(Constants.RequestParametersJsonKeys.LOCALE, reportRequestConfig.locale)
                .put(Constants.RequestParametersJsonKeys.ROOT_STATUS, reportRequestConfig.deviceRootStatus)
                .put(Constants.RequestParametersJsonKeys.APP_DEBUGGABLE, reportRequestConfig.isAppDebuggable)
                .put(Constants.RequestParametersJsonKeys.APP_FRAMEWORK, reportRequestConfig.appFramework)
                .put(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, reportRequestConfig.attributionId)
                .put(
                    Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME,
                    reportRequestConfig.analyticsSdkVersionName
                )
                .put(Constants.RequestParametersJsonKeys.KIT_BUILD_NUMBER, reportRequestConfig.kitBuildNumber)
        } catch (exception: Throwable) {
            YLogger.error(tag, exception, "Something was wrong while filling request parameters.")
            JSONObject()
        }

        YLogger.info(tag, "SessionRequestParameters in fill report request parameters $requestParameters")
        return requestParameters.toString()
    }
}
