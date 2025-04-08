package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
     * Creates a new builder for {@link AppMetricaLibraryAdapterConfig}.
     * @return a new builder for {@link AppMetricaLibraryAdapterConfig}.
     */
    public static Builder newConfigBuilder() {
        return new Builder();
    }

    @NonNull
    @Override
    public String toString() {
        return "AppMetricaLibraryAdapterConfig{" +
            "advIdentifiersTracking=" + advIdentifiersTracking +
            '}';
    }

    private AppMetricaLibraryAdapterConfig(@NonNull Builder builder) {
        this.advIdentifiersTracking = builder.advIdentifiersTracking;
    }

    /**
     * Builds a new {@link AppMetricaLibraryAdapterConfig} object.
     */
    public static class Builder {

        @Nullable
        private Boolean advIdentifiersTracking;

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
