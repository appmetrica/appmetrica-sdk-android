package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.impl.service.ServiceDataReporter;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom event parameters.
 */
public final class ModuleEvent {

    private final int type;
    @Nullable
    private final String name;
    @Nullable
    private final String value;
    private final int serviceDataReporterType;
    @Nullable
    private final Map<String, Object> environment;
    @Nullable
    private final Map<String, byte[]> extras;
    @Nullable
    private final Map<String, Object> attributes;

    /**
     * @return event type
     */
    public int getType() {
        return type;
    }

    /**
     * @return event name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @return event value
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * @return the way this event will be tracked
     */
    public int getServiceDataReporterType() {
        return serviceDataReporterType;
    }

    /**
     * @return event environment
     */
    @Nullable
    public Map<String, Object> getEnvironment() {
        return environment;
    }

    /**
     * @return event extras
     */
    @Nullable
    public Map<String, byte[]> getExtras() {
        return extras;
    }

    /**
     * @return event attributes
     */
    @Nullable
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    private ModuleEvent(@NonNull final Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.value = builder.value;
        this.serviceDataReporterType = builder.serviceDataReporterType;
        this.environment = builder.environment;
        this.extras = builder.extras;
        this.attributes = builder.attributes;
    }

    /**
     * Creates new instance of {@link Builder}.
     *
     * @param type event type
     * @return instance of {@link Builder}
     */
    public static Builder newBuilder(final int type) {
        return new Builder(type);
    }

    @NonNull
    @Override
    public String toString() {
        return "ModuleEvent{" +
            "type=" + type +
            ", name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", serviceDataReporterType=" + serviceDataReporterType +
            ", environment=" + environment +
            ", extras=" + extras +
            ", attributes=" + attributes +
            '}';
    }

    /**
     * Builds a new {@link ModuleEvent} object.
     */
    public static class Builder {

        private final int type;
        @Nullable
        private String name;
        @Nullable
        private String value;
        private int serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE;
        @Nullable
        private Map<String, Object> environment;
        @Nullable
        private Map<String, byte[]> extras;
        @Nullable
        private Map<String, Object> attributes;

        private Builder(final int type) {
            this.type = type;
        }

        /**
         * Sets event name.
         *
         * @param name {@link String} value of event name
         * @return same {@link Builder} object
         */
        public Builder withName(@Nullable final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets event value.
         *
         * @param value {@link String} value of event value
         * @return same {@link Builder} object
         */
        public Builder withValue(@Nullable final String value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the way event is processed.
         *
         * @param serviceDataReporterType type of {@link ServiceDataReporter}
         * @return same {@link Builder} object
         */
        public Builder withServiceDataReporterType(final int serviceDataReporterType) {
            this.serviceDataReporterType = serviceDataReporterType;
            return this;
        }

        /**
         * Sets event environment.
         *
         * @param environment map with environment keys and values
         * @return same {@link Builder} object
         */
        public Builder withEnvironment(@Nullable final Map<String, Object> environment) {
            if (environment != null) {
                this.environment = new HashMap<>(environment);
            }
            return this;
        }

        /**
         * Sets event extras.
         *
         * @param extras map with extras keys and values
         * @return same {@link Builder} object
         */
        public Builder withExtras(@Nullable final Map<String, byte[]> extras) {
            if (extras != null) {
                this.extras = new HashMap<>(extras);
            }
            return this;
        }

        /**
         * Sets event attributes.
         *
         * @param attributes map with attributes keys and values
         * @return same {@link Builder} object
         */
        public Builder withAttributes(@Nullable final Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes = new HashMap<>(attributes);
            }
            return this;
        }

        /**
         * Creates instance of {@link ModuleEvent}
         *
         * @return {@link ModuleEvent} object
         */
        public ModuleEvent build() {
            return new ModuleEvent(this);
        }
    }
}
