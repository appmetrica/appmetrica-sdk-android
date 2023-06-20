package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.utils.JsonHelper;

public class DbNetworkTaskConfig {

    @Nullable public final String deviceId;
    @Nullable public final String uuid;
    @Nullable public final String analyticsSdkVersionName;
    @Nullable public final String kitBuildNumber;
    @Nullable public final String kitBuildType;
    @Nullable public final String appVersion;
    @Nullable public final String appDebuggable;
    @Nullable public final String appBuildNumber;
    @Nullable public final String osVersion;
    @Nullable public final String osApiLevel;
    @Nullable public final String locale;
    @Nullable public final String deviceRootStatus;
    @Nullable public final String appFramework;
    @Nullable public final String attributionId;

    public DbNetworkTaskConfig(@NonNull JsonHelper.OptJSONObject requestParameters) {
        deviceId = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.DEVICE_ID);
        uuid = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.UUID);
        analyticsSdkVersionName =
                requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME);
        kitBuildNumber = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.KIT_BUILD_NUMBER);
        kitBuildType = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.KIT_BUILD_TYPE);
        appVersion = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.APP_VERSION);
        appDebuggable = requestParameters.optString(Constants.RequestParametersJsonKeys.APP_DEBUGGABLE, "0");
        appBuildNumber = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.APP_BUILD);
        osVersion = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.OS_VERSION);
        locale = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.LOCALE);
        deviceRootStatus = requestParameters.getStringOrEmpty(Constants.RequestParametersJsonKeys.ROOT_STATUS);
        appFramework = requestParameters.optString(
                Constants.RequestParametersJsonKeys.APP_FRAMEWORK,
                FrameworkDetector.framework()
        );

        final int defaultOsApiLevel = -1;
        int osApiLevelFromDB =
                requestParameters.optInt(Constants.RequestParametersJsonKeys.OS_API_LEVEL, defaultOsApiLevel);
        osApiLevel = osApiLevelFromDB == defaultOsApiLevel ? null : String.valueOf(osApiLevelFromDB);
        int attributionIdFromDB = requestParameters.optInt(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, 0);
        attributionId = attributionIdFromDB > 0 ? String.valueOf(attributionIdFromDB) : null;
    }

    public DbNetworkTaskConfig() {
        deviceId = null;
        uuid = null;
        analyticsSdkVersionName = null;
        kitBuildNumber = null;
        kitBuildType = null;
        appVersion = null;
        appDebuggable = null;
        appBuildNumber = null;
        osVersion = null;
        osApiLevel = null;
        locale = null;
        deviceRootStatus = null;
        appFramework = null;
        attributionId = null;
    }

    @Override
    public String toString() {
        return "DbNetworkTaskConfig{" +
                "deviceId='" + deviceId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", analyticsSdkVersionName='" + analyticsSdkVersionName + '\'' +
                ", kitBuildNumber='" + kitBuildNumber + '\'' +
                ", kitBuildType='" + kitBuildType + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", appDebuggable='" + appDebuggable + '\'' +
                ", appBuildNumber='" + appBuildNumber + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", osApiLevel='" + osApiLevel + '\'' +
                ", locale='" + locale + '\'' +
                ", deviceRootStatus='" + deviceRootStatus + '\'' +
                ", appFramework='" + appFramework + '\'' +
                ", attributionId='" + attributionId + '\'' +
                '}';
    }
}
