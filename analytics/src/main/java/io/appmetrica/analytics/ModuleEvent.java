package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter;

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

    public int getType() {
        return type;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public int getServiceDataReporterType() {
        return serviceDataReporterType;
    }

    @Nullable
    public Map<String, Object> getEnvironment() {
        return environment;
    }

    @Nullable
    public Map<String, byte[]> getExtras() {
        return extras;
    }

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

    public static class Builder {

        private final int type;
        @Nullable
        private String name;
        @Nullable
        private String value;
        private int serviceDataReporterType = MetricaServiceDataReporter.TYPE_CORE;
        @Nullable
        private Map<String, Object> environment;
        @Nullable
        private Map<String, byte[]> extras;
        @Nullable
        private Map<String, Object> attributes;

        private Builder(final int type) {
            this.type = type;
        }

        public Builder withName(@Nullable final String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(@Nullable final String value) {
            this.value = value;
            return this;
        }

        public Builder withServiceDataReporterType(final int serviceDataReporterType) {
            this.serviceDataReporterType = serviceDataReporterType;
            return this;
        }

        public Builder withEnvironment(@Nullable final Map<String, Object> environment) {
            if (environment != null) {
                this.environment = new HashMap<>(environment);
            }
            return this;
        }

        public Builder withExtras(@Nullable final Map<String, byte[]> extras) {
            if (extras != null) {
                this.extras = new HashMap<>(extras);
            }
            return this;
        }

        public Builder withAttributes(@Nullable final Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes = new HashMap<>(attributes);
            }
            return this;
        }

        public ModuleEvent build() {
            return new ModuleEvent(this);
        }
    }
}
