package io.appmetrica.analytics.impl;

import android.location.Location;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientConfigSerializer {

    private static final String KEY_API_KEY = "apikey";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_SESSION_TIMEOUT = "session_timeout";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PRELOAD_INFO = "preload_info";
    private static final String KEY_LOGS = "logs";
    private static final String KEY_CRASH_ENABLED = "crash_enabled";
    private static final String KEY_CRASH_NATIVE_ENABLED = "crash_native_enabled";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_PRELOAD_INFO_TRACKID = "trackid";
    private static final String KEY_PRELOAD_INFO_PARAMS = "params";
    private static final String KEY_LOCATION_PROVIDER = "provider";
    private static final String KEY_LOCATION_TIME = "time";
    private static final String KEY_LOCATION_ACCURACY = "accuracy";
    private static final String KEY_LOCATION_ALT = "alt";
    private static final String KEY_LOCATION_LNG = "lng";
    private static final String KEY_LOCATION_LAT = "lat";
    private static final String KEY_MAX_REPORTS_IN_DB_COUNT = "max_reports_in_db_count";
    private static final String KEY_ERROR_ENVIRONMENT = "error_environment";
    private static final String KEY_FIRST_ACTIVATION_AS_UPDATE = "first_activation_as_update";
    private static final String KEY_DATA_SENDING_ENABLED = "data_sending_enabled";
    private static final String KEY_USER_PROFILE_ID = "user_profile_id";
    private static final String KEY_REVENUE_AUTO_TRACKING_ENABLED = "revenue_auto_tracking_enabled";
    private static final String KEY_SESSIONS_AUTO_TRACKING_ENABLED = "sessions_auto_tracking_enabled";
    private static final String KEY_APP_OPEN_TRACKING_ENABLED = "app_open_tracking_enabled";
    private static final String KEY_DEVICE_TYPE = "device_type";
    private static final String KEY_APP_BUILD_NUMBER = "app_build_number";
    private static final String KEY_DISPATCH_PERIOD_SECONDS = "dispatch_period_seconds";
    private static final String KEY_MAX_REPORTS_COUNT = "max_reports_count";
    private static final String KEY_APP_ENVIRONMENT = "app_environment";
    private static final String KEY_ANR_MONITORING = "anr_monitoring";
    private static final String KEY_ANR_MONITORING_TIMEOUT = "anr_monitoring_timeout";
    private static final String KEY_CUSTOM_HOSTS = "customHosts";
    private static final String KEY_ADDITIONAL_CONFIG = "additional_config";

    public ClientConfigSerializer() {}

    public String toJson(final AppMetricaConfig config) {
        try {
            JSONObject jsonConfig = new JSONObject();
            jsonConfig.put(KEY_API_KEY, config.apiKey);
            jsonConfig.put(KEY_APP_VERSION, config.appVersion);
            jsonConfig.put(KEY_SESSION_TIMEOUT, config.sessionTimeout);
            jsonConfig.put(KEY_LOCATION, locationToJson(config.location));
            jsonConfig.put(KEY_PRELOAD_INFO, preloadInfoToJson(config.preloadInfo));
            jsonConfig.put(KEY_LOGS, config.logs);
            jsonConfig.put(KEY_CRASH_ENABLED, config.crashReporting);
            jsonConfig.put(KEY_CRASH_NATIVE_ENABLED, config.nativeCrashReporting);
            jsonConfig.put(KEY_LOCATION_ENABLED, config.locationTracking);
            jsonConfig.put(KEY_MAX_REPORTS_IN_DB_COUNT, config.maxReportsInDatabaseCount);
            jsonConfig.put(KEY_ERROR_ENVIRONMENT, JsonHelper.mapToJson(config.errorEnvironment));
            jsonConfig.put(KEY_FIRST_ACTIVATION_AS_UPDATE, config.firstActivationAsUpdate);
            jsonConfig.put(KEY_DATA_SENDING_ENABLED, config.dataSendingEnabled);
            jsonConfig.put(KEY_USER_PROFILE_ID, config.userProfileID);
            jsonConfig.put(KEY_REVENUE_AUTO_TRACKING_ENABLED, config.revenueAutoTrackingEnabled);
            jsonConfig.put(KEY_SESSIONS_AUTO_TRACKING_ENABLED, config.sessionsAutoTrackingEnabled);
            jsonConfig.put(KEY_APP_OPEN_TRACKING_ENABLED, config.appOpenTrackingEnabled);
            jsonConfig.put(KEY_DEVICE_TYPE, config.deviceType);
            jsonConfig.put(KEY_APP_BUILD_NUMBER, config.appBuildNumber);
            jsonConfig.put(KEY_DISPATCH_PERIOD_SECONDS, config.dispatchPeriodSeconds);
            jsonConfig.put(KEY_MAX_REPORTS_COUNT, config.maxReportsCount);
            jsonConfig.put(KEY_APP_ENVIRONMENT, JsonHelper.mapToJson(config.appEnvironment));
            jsonConfig.put(KEY_ANR_MONITORING, config.anrMonitoring);
            jsonConfig.put(KEY_ANR_MONITORING_TIMEOUT, config.anrMonitoringTimeout);
            if (config.customHosts != null) {
                jsonConfig.put(KEY_CUSTOM_HOSTS, JsonHelper.listToJson(config.customHosts));
            }
            jsonConfig.put(KEY_ADDITIONAL_CONFIG, JsonHelper.mapToJson(config.additionalConfig));

            return jsonConfig.toString();
        } catch (Throwable e) {}
        return "";
    }

    @SuppressWarnings("checkstyle:methodlength")
    public AppMetricaConfig fromJson(final String json) {
        AppMetricaConfig config = null;
        if (TextUtils.isEmpty(json) == false) {
            try {
                JSONObject jsonConfig = new JSONObject(json);
                final AppMetricaConfig.Builder builder =
                        AppMetricaConfig.newConfigBuilder(jsonConfig.getString(KEY_API_KEY));

                if (jsonConfig.has(KEY_APP_VERSION)) {
                    builder.withAppVersion(jsonConfig.optString(KEY_APP_VERSION));
                }
                if (jsonConfig.has(KEY_SESSION_TIMEOUT)) {
                    builder.withSessionTimeout(jsonConfig.getInt(KEY_SESSION_TIMEOUT));
                }
                builder.withLocation(jsonToLocation(jsonConfig.optString(KEY_LOCATION)));
                builder.withPreloadInfo(jsonToPreloadInfo(jsonConfig.optString(KEY_PRELOAD_INFO)));
                if (jsonConfig.has(KEY_LOGS) && jsonConfig.optBoolean(KEY_LOGS)) {
                    builder.withLogs();
                }
                if (jsonConfig.has(KEY_CRASH_ENABLED)) {
                    builder.withCrashReporting(jsonConfig.optBoolean(KEY_CRASH_ENABLED));
                }
                if (jsonConfig.has(KEY_CRASH_NATIVE_ENABLED)) {
                    builder.withNativeCrashReporting(jsonConfig.optBoolean(KEY_CRASH_NATIVE_ENABLED));
                }
                if (jsonConfig.has(KEY_LOCATION_ENABLED)) {
                    builder.withLocationTracking(jsonConfig.optBoolean(KEY_LOCATION_ENABLED));
                }
                if (jsonConfig.has(KEY_MAX_REPORTS_IN_DB_COUNT)) {
                    builder.withMaxReportsInDatabaseCount(jsonConfig.optInt(KEY_MAX_REPORTS_IN_DB_COUNT));
                }
                if (jsonConfig.has(KEY_ERROR_ENVIRONMENT)) {
                    withErrorEnvironmentValues(jsonConfig.optString(KEY_ERROR_ENVIRONMENT), builder);
                }
                if (jsonConfig.has(KEY_FIRST_ACTIVATION_AS_UPDATE)) {
                    builder.handleFirstActivationAsUpdate(jsonConfig.optBoolean(KEY_FIRST_ACTIVATION_AS_UPDATE));
                }
                if (jsonConfig.has(KEY_DATA_SENDING_ENABLED)) {
                    builder.withDataSendingEnabled(jsonConfig.optBoolean(KEY_DATA_SENDING_ENABLED));
                }
                if (jsonConfig.has(KEY_USER_PROFILE_ID)) {
                    builder.withUserProfileID(jsonConfig.optString(KEY_USER_PROFILE_ID, null));
                }
                if (jsonConfig.has(KEY_REVENUE_AUTO_TRACKING_ENABLED)) {
                    builder.withRevenueAutoTrackingEnabled(jsonConfig.optBoolean(KEY_REVENUE_AUTO_TRACKING_ENABLED));
                }
                if (jsonConfig.has(KEY_SESSIONS_AUTO_TRACKING_ENABLED)) {
                    builder.withSessionsAutoTrackingEnabled(jsonConfig.optBoolean(KEY_SESSIONS_AUTO_TRACKING_ENABLED));
                }
                if (jsonConfig.has(KEY_APP_OPEN_TRACKING_ENABLED)) {
                    builder.withAppOpenTrackingEnabled(jsonConfig.optBoolean(KEY_APP_OPEN_TRACKING_ENABLED));
                }
                if (jsonConfig.has(KEY_DEVICE_TYPE)) {
                    builder.withDeviceType(jsonConfig.optString(KEY_DEVICE_TYPE));
                }
                if (jsonConfig.has(KEY_APP_BUILD_NUMBER)) {
                    builder.withAppBuildNumber(jsonConfig.optInt(KEY_APP_BUILD_NUMBER));
                }
                if (jsonConfig.has(KEY_DISPATCH_PERIOD_SECONDS)) {
                    builder.withDispatchPeriodSeconds(jsonConfig.optInt(KEY_DISPATCH_PERIOD_SECONDS));
                }
                if (jsonConfig.has(KEY_MAX_REPORTS_COUNT)) {
                    builder.withMaxReportsCount(jsonConfig.optInt(KEY_MAX_REPORTS_COUNT));
                }
                if (jsonConfig.has(KEY_APP_ENVIRONMENT)) {
                    withAppEnvironmentValues(jsonConfig.optString(KEY_APP_ENVIRONMENT), builder);
                }
                if (jsonConfig.has(KEY_ANR_MONITORING)) {
                    builder.withAnrMonitoring(jsonConfig.optBoolean(KEY_ANR_MONITORING));
                }
                if (jsonConfig.has(KEY_ANR_MONITORING_TIMEOUT)) {
                    builder.withAnrMonitoringTimeout(jsonConfig.optInt(KEY_ANR_MONITORING_TIMEOUT));
                }
                if (jsonConfig.has(KEY_CUSTOM_HOSTS)) {
                    builder.withCustomHosts(JsonHelper.toStringList(jsonConfig.optJSONArray(KEY_CUSTOM_HOSTS)));
                }
                if (jsonConfig.has(KEY_ADDITIONAL_CONFIG)) {
                    withAdditionalConfigValues(jsonConfig.optString(KEY_ADDITIONAL_CONFIG), builder);
                }
                config = builder.build();
            } catch (Throwable e) {
                YLogger.e(e, "Exception while serializing AppMetricaConfig to json.");
            }
        }
        return config;
    }

    private void withErrorEnvironmentValues(@NonNull String json, @NonNull AppMetricaConfig.Builder builder) {
        Map<String, String> environmentMap = JsonHelper.jsonToMap(json);
        if (environmentMap != null) {
            for (Map.Entry<String, String> entry : environmentMap.entrySet()) {
                builder.withErrorEnvironmentValue(entry.getKey(), entry.getValue());
            }
        }
    }

    private void withAppEnvironmentValues(@NonNull String json, @NonNull AppMetricaConfig.Builder builder) {
        Map<String, String> environmentMap = JsonHelper.jsonToMap(json);
        if (environmentMap != null) {
            for (Map.Entry<String, String> entry : environmentMap.entrySet()) {
                builder.withAppEnvironmentValue(entry.getKey(), entry.getValue());
            }
        }
    }

    private void withAdditionalConfigValues(@NonNull String json, @NonNull AppMetricaConfig.Builder builder) {
        Map<String, String> map = JsonHelper.jsonToMap(json);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.withAdditionalConfig(entry.getKey(), entry.getValue());
            }
        }
    }

    private String preloadInfoToJson(final PreloadInfo preloadInfo) {
        if (preloadInfo == null) {
            return null;
        }
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_PRELOAD_INFO_TRACKID, preloadInfo.getTrackingId());
            json.put(KEY_PRELOAD_INFO_PARAMS, JsonHelper.mapToJson(preloadInfo.getAdditionalParams()));
            return json.toString();
        } catch (Throwable e) {}
        return null;
    }

    private PreloadInfo jsonToPreloadInfo(final String preloadInfo) throws JSONException {
        PreloadInfo info = null;
        if (TextUtils.isEmpty(preloadInfo) == false) {
            JSONObject jsonInfo = new JSONObject(preloadInfo);
            String trackingId = null;
            if (jsonInfo.has(KEY_PRELOAD_INFO_TRACKID)) {
                trackingId = jsonInfo.optString(KEY_PRELOAD_INFO_TRACKID);
            }

            PreloadInfo.Builder builder = PreloadInfo.newBuilder(trackingId);
            HashMap<String, String> params = JsonHelper.jsonToMap(jsonInfo.optString(KEY_PRELOAD_INFO_PARAMS));
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.setAdditionalParams(entry.getKey(), entry.getValue());
                }
            }
            info = builder.build();
        }
        return info;
    }

    private String locationToJson(final Location location) {
        if (location == null) {
            return null;
        }
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_LOCATION_PROVIDER, location.getProvider());
            json.put(KEY_LOCATION_TIME, location.getTime());
            json.put(KEY_LOCATION_ACCURACY, location.getAccuracy());
            json.put(KEY_LOCATION_ALT, location.getAltitude());
            json.put(KEY_LOCATION_LNG, location.getLongitude());
            json.put(KEY_LOCATION_LAT, location.getLatitude());
            return json.toString();
        } catch (Throwable e) {}
        return null;
    }

    private Location jsonToLocation(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            String provider = null;
            if (jsonObject.has(KEY_LOCATION_PROVIDER)) {
                provider = jsonObject.optString(KEY_LOCATION_PROVIDER);
            }
            Location location = new Location(provider);
            location.setLongitude(jsonObject.getDouble(KEY_LOCATION_LNG));
            location.setLatitude(jsonObject.getDouble(KEY_LOCATION_LAT));
            location.setTime(jsonObject.optLong(KEY_LOCATION_TIME));
            location.setAccuracy((float) jsonObject.optDouble(KEY_LOCATION_ACCURACY));
            location.setAltitude((float) jsonObject.optDouble(KEY_LOCATION_ALT));
            return location;
        } catch (Throwable e) {}
        return null;
    }

}
