package io.appmetrica.analytics;

/**
 * Status of {@link StartupParamsItem}.
 */
public enum StartupParamsItemStatus {
    /**
     * Value is present.
     */
    OK,
    /**
     * Value is absent because provider is unavailable.
     */
    PROVIDER_UNAVAILABLE,
    /**
     * Value is absent because it is invalid.
     */
    INVALID_VALUE_FROM_PROVIDER,
    /**
     * Value is absent because some network error happened.
     */
    NETWORK_ERROR,
    /**
     * Value is absent because feature is disabled.
     */
    FEATURE_DISABLED,
    /**
     * Value is absent because some unknown error happened.
     */
    UNKNOWN_ERROR,
    /**
     * Value is absent because forbidden by client config.
     */
    FORBIDDEN_BY_CLIENT_CONFIG,
}
