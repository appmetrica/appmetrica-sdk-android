package io.appmetrica.analytics.impl.request;

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.networktasks.internal.CommonUrlParts;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UrlParts {
    public static final String STARTUP_PATH = "analytics/startup";
    public static final String REPORT_PATH = "report";
    public static final String DIAGNOSTIC_PATH = "diagnostic";
    public static final String QUERIES = "queries";
    public static final String QUERY_HOSTS = "query_hosts";
    public static final String API_KEY = "api_key";
    public static final String API_KEY_128 = "api_key_128";
    public static final String DISTRIBUTION_CUSTOMIZATION = "distribution_customization";
    public static final String CLIDS_SET = "clids_set";
    public static final String CLIDS_SET_SOURCE = "clids_set_source";
    public static final String DISTRIBUTION_REFERRER = "install_referrer";
    public static final String FEATURES = "features";
    public static final String STARTUP_UPDATE = "startup_update";
    public static final String DETECT_LOCALE = "detect_locale";
    public static final String PERMISSIONS_COLLECTING = "permissions_collecting";
    public static final String FEATURES_COLLECTING = "features_collecting";
    public static final String APP_DEBUGGABLE = "app_debuggable";
    public static final String FEATURE_GOOGLE_AID = "google_aid";
    public static final String FEATURE_HUAWEI_OAID = "huawei_oaid";
    public static final String FEATURE_SIM_INFO = "sim_info";
    public static final String FEATURE_SSL_PINNING = "ssl_pinning";
    public static final String TIME = "time";
    public static final String ATTRIBUTION_ID = "attribution_id";
    public static final String COUNTRY_INIT = "country_init";
    public static final String STAT_SENDING = "stat_sending";
    public static final String RETRY_POLICY = "retry_policy";
    public static final String APP_SYSTEM = "app_system_flag";
    public static final String INSTALL_REFERRER_SOURCE = "install_referrer_source";
    public static final String CACHE_CONTROL = "cache_control";
    public static final String AUTO_INAPP_COLLECTING = "auto_inapp_collecting";
    public static final String ATTRIBUTION = "attribution";
    public static final String EXTERNAL_ATTRIBUTION = "external_attribution";

    public static final Map<AdTrackingInfo.Provider, String> ADV_ID_PROVIDER_TO_PARAMETER;
    public static final Map<AdTrackingInfo.Provider, String> LIMITED_AD_TRACKING_PROVIDER_TO_PARAMETER;

    static {
        HashMap<AdTrackingInfo.Provider, String> idMapping = new HashMap<AdTrackingInfo.Provider, String>();
        idMapping.put(AdTrackingInfo.Provider.GOOGLE, CommonUrlParts.ADV_ID);
        idMapping.put(AdTrackingInfo.Provider.HMS, CommonUrlParts.HUAWEI_OAID);
        idMapping.put(AdTrackingInfo.Provider.YANDEX, CommonUrlParts.YANDEX_ADV_ID);
        ADV_ID_PROVIDER_TO_PARAMETER = Collections.unmodifiableMap(idMapping);

        HashMap<AdTrackingInfo.Provider, String> limitedMapping = new HashMap<AdTrackingInfo.Provider, String>();
        limitedMapping.put(AdTrackingInfo.Provider.GOOGLE,
            CommonUrlParts.LIMIT_AD_TRACKING);
        limitedMapping.put(AdTrackingInfo.Provider.HMS,
            CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING);
        limitedMapping.put(AdTrackingInfo.Provider.YANDEX,
            CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING);
        LIMITED_AD_TRACKING_PROVIDER_TO_PARAMETER = Collections.unmodifiableMap(limitedMapping);
    }
}
