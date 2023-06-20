package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SessionFactoryArguments {

    @NonNull private final SessionType mType;
    @Nullable private final Integer mSessionTimeout;

    private SessionFactoryArguments(Builder builder) {
        mType = builder.type;
        mSessionTimeout = builder.sessionTimeout;
    }

    public static final Builder newBuilder(SessionType type) {
        return new Builder(type);
    }

    @NonNull public SessionType getType() {
        return mType;
    }

    @Nullable public Integer getSessionTimeout() {
        return mSessionTimeout;
    }

    static final class Builder {

        @NonNull private SessionType type;
        @Nullable private Integer sessionTimeout;

        private Builder(SessionType type) {
            this.type = type;
        }

        public Builder withSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public SessionFactoryArguments build() {
            return new SessionFactoryArguments(this);
        }

    }
}
