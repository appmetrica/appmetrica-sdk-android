package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.List;

/**
 * Contains configuration for AppMetricaLibraryAdapter.
 * Configuration created by {@link Builder}.
 */
public class AppMetricaLibraryAdapterConfig {

    /**
     * <p>Indicates whether AppMetrica should include advertising identifiers withing its reports</p>
     *
     * <p>@{code true} if allow AppMetrica to record advertising identifiers in reports, otherwise {@code false}</p>
     */
    @Nullable
    public final Boolean advIdentifiersTracking;

    /**
     * Custom hosts for startup config.
     *
     * @see AppMetricaLibraryAdapterConfig.Builder#withCustomHosts(List)
     */
    @Nullable
    public final List<String> customHosts;

    /**
     * Creates a new builder for {@link AppMetricaLibraryAdapterConfig}.
     * @return a new builder for {@link AppMetricaLibraryAdapterConfig}.
     */
    public static Builder newConfigBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AppMetricaLibraryAdapterConfig{" +
            "advIdentifiersTracking=" + advIdentifiersTracking +
            ", customHosts=" + customHosts +
            '}';
    }

    private AppMetricaLibraryAdapterConfig(@NonNull Builder builder) {
        this.advIdentifiersTracking = builder.advIdentifiersTracking;
        this.customHosts = builder.customHosts;
    }

    /**
     * Builds a new {@link AppMetricaLibraryAdapterConfig} object.
     */
    public static class Builder {

        /** Creates a new {@link Builder} instance. */
        public Builder() {}

        @Nullable
        private Boolean advIdentifiersTracking;
        @Nullable
        private List<String> customHosts;

        /**
         * Enables/disables including advertising identifiers like GAID, Huawei OAID within its reports.
         *
         * @param enabled {@code true} to allow AppMetrica to record advertising identifiers information in reports,
         *                            otherwise {@code false}.
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         *
         * @see AppMetrica#setAdvIdentifiersTracking(boolean)
         * @see AppMetricaConfig#advIdentifiersTracking
         *
         */
        @NonNull
        public Builder withAdvIdentifiersTracking(boolean enabled) {
            advIdentifiersTracking = enabled;
            return this;
        }

        /**
         * Sets the list of hosts to be used for startup requests. This is optional value.
         *
         * @param customHosts non-empty host list.
         *
         * @return the same {@link Builder} object.
         *
         * @see AppMetricaLibraryAdapter#setCustomHosts(String[])
         * @see AppMetricaLibraryAdapterConfig#customHosts
         */
        @NonNull
        public Builder withCustomHosts(@NonNull List<String> customHosts) {
            this.customHosts = CollectionUtils.unmodifiableListCopy(customHosts);
            return this;
        }

        /**
         * Creates instance of {@link AppMetricaLibraryAdapterConfig}
         *
         * @return {@link AppMetricaLibraryAdapterConfig} object
         */
        @NonNull
        public AppMetricaLibraryAdapterConfig build() {
            return new AppMetricaLibraryAdapterConfig(this);
        }
    }
}
