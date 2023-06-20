package io.appmetrica.analytics;

import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains information for tracking preloaded apps
 * Configuration created by {@link Builder}
 */
public class PreloadInfo {

    /**
     * Builds a new {@link PreloadInfo} instance.
     */
    public static class Builder {
        private String mTrackingId;
        private Map<String, String> mAdditionalParams;

        private Builder(String trackingId) {
            mTrackingId = trackingId;
            mAdditionalParams = new HashMap<String, String>();
        }

        /**
         * Sets additional parameters for tracking preloaded apps. Can be called many times for different
         * key-pairs.
         *
         * @param key The key of key-value pair of additional parameters. Cannot be null. The pair with null-key will
         *            be ignored.
         *
         * @param value The value of key-value pair of additional parameters. Cannot be null. The pair with
         *              null-value will be ignored.
         *
         * @return The same {@link Builder} instance.
         *
         * @see PreloadInfo#getAdditionalParams()
         */
        public Builder setAdditionalParams(String key, String value) {
            if (key != null && value != null) {
                mAdditionalParams.put(key, value);
            }
            return this;
        }

        /**
         * Creates a new instances of {@link PreloadInfo} with defined configuration.
         *
         * @return {@link PreloadInfo} object.
         */
        public PreloadInfo build() {
            return new PreloadInfo(this);
        }
    }

    private String mTrackingId;
    private Map<String, String> mAdditionalParams;

    private PreloadInfo(Builder builder) {
        mTrackingId = builder.mTrackingId;
        mAdditionalParams = CollectionUtils.unmodifiableMapCopy(builder.mAdditionalParams);
    }

    /**
     * Creates a new instance of {@link PreloadInfo.Builder}.
     *
     * @param trackingId - The Tracking Id for tracking preloaded apps.
     *
     * @return The builder of {@link PreloadInfo}.
     *
     * @see PreloadInfo#getTrackingId()
     */
    public static Builder newBuilder(String trackingId) {
        return new Builder(trackingId);
    }

    /**
     * Return the Tracking Id for tracking preloaded apps.
     *
     * @return the Tracking Id value.
     *
     * @see PreloadInfo#newBuilder(String)
     */
    public String getTrackingId() {
        return mTrackingId;
    }

    /**
     * Return the additional parameters for tracking preloaded apps which can't be modified.
     *
     * @return the unmodifiable {@link Map} of key-value pairs.
     *
     * @see Builder#setAdditionalParams(String, String)
     * @see Collections#unmodifiableMap(Map)
     */
    public Map<String, String> getAdditionalParams() {
        return mAdditionalParams;
    }
}
