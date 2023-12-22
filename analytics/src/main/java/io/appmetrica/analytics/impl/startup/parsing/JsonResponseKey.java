package io.appmetrica.analytics.impl.startup.parsing;

public final class JsonResponseKey {

    // Specific keys. Related to identifiers
    static final String DEVICE_ID = "device_id";
    static final String HASH = "hash";

    // Specific keys. Related to urls/hosts for metrica analytics & ads
    static final String QUERIES = "queries";
    static final String QUERY_HOSTS = "query_hosts";
    static final String STARTUP_HOSTS = "startup";
    static final String SEND_REPORTS_HOSTS = "report";
    static final String REPORT_AD_HOST = "report_ad";
    static final String GET_AD_HOST = "get_ad";
    static final String DIAGNOSTIC_HOSTS = "diagnostic";
    static final String CERTIFICATE = "host";

    static final String QUERY_DISTRIBUTION_CUSTOMIZATION = "distribution_customization";
    static final String QUERY_CLIDS = "clids";

    // Common keys
    static final String URL = "url";
    static final String URLS = "urls";
    static final String VALUE = "value";
    static final String LIST = "list";

    //region Features
    static final String FEATURES = "features";
    static final String FEATURE_ENABLED = "enabled";
    static final String FEATURE_PERMISSION_COLLECTING = "permissions_collecting";
    static final String FEATURE_FEATURES_COLLECTING = "features_collecting";
    static final String FEATURE_GOOGLE_AID = "google_aid";
    static final String FEATURE_SIM_INFO = "sim_info";
    static final String FEATURE_HUAWEI_OAID = "huawei_oaid";
    static final String FEATURE_SSL_PINNING = "ssl_pinning";
    //endregion

    //region Time
    static final String TIME = "time";
    static final String MAX_VALID_DIFFERENCE_SECONDS = "max_valid_difference_seconds";

    //endregion

    //region Locale
    static final String LOCALE = "locale";
    static final String COUNTRY = "country";
    static final String RELIABLE = "reliable";
    //endregion

    //region Permissions
    static final String PERMISSIONS_COLLECTING_CONFIG = "permissions_collecting";
    static final String CHECK_INTERVAL_SECONDS = "check_interval_seconds";
    static final String FORCE_SEND_INTERVAL_SECONDS = "force_send_interval_seconds";
    //endregion

    //region StatSending
    static final String STAT_SENDING = "stat_sending";
    static final String DISABLED_REPORTING_INTERVAL_SECONDS = "disabled_reporting_interval_seconds";
    //endregion

    //region retry policy
    static final String RETRY_POLICY_CONFIG = "retry_policy";
    static final String RETRY_POLICY_MAX_INTERVAL = "max_interval_seconds";
    static final String RETRY_POLICY_EXPONENTIAL_MULTIPLIER = "exponential_multiplier";
    //endregion

    //region CacheControl
    static final String CACHE_CONTROL = "cache_control";
    static final String LAST_KNOWN_LOCATION_TTL = "last_known_location_ttl";
    //endregion

    //region AutoInappCollecting
    static final String AUTO_INAPP_COLLECTING = "auto_inapp_collecting";
    static final String SEND_FREQUENCY_SECONDS = "send_frequency_seconds";
    static final String FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS = "first_collecting_inapp_max_age_seconds";
    //endregion

    //region StartupUpdate
    public static final String STARTUP_UPDATE = "startup_update";
    public static final String STARTUP_UPDATE_INTERVAL_SECONDS = "interval_seconds";
    //endregion

    //region ExternalAttribution
    static final String EXTERNAL_ATTRIBUTION = "external_attribution";
    static final String COLLECTING_INTERVAL_SECONDS = "collecting_interval_seconds";
    //endregion
}
