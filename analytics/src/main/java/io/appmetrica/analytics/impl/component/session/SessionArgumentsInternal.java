package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SessionArgumentsInternal {

    @NonNull
    private final SessionType mType;

    @Nullable
    private final Long mId;
    @Nullable
    private final Long mCreationTime;
    @Nullable
    private final Integer mSessionTimeout;
    @Nullable
    private final Long mCurrentReportId;
    @Nullable
    private final Boolean mAliveNeeded;
    @Nullable
    private final Long mSleepStart;
    @Nullable
    private final Long mLastEventOffset;

    private SessionArgumentsInternal(SessionArgumentsInternal.Builder builder) {
        mType = builder.type;
        mSessionTimeout = builder.timeout;
        mId = builder.id;
        mCreationTime = builder.creationTime;
        mCurrentReportId = builder.currentReportId;
        mAliveNeeded = builder.aliveNeeded;
        mSleepStart = builder.sleepStart;
        mLastEventOffset = builder.lastEventOffset;
    }

    public static final Builder newBuilder(SessionFactoryArguments arguments) {
        return new Builder(arguments);
    }

    public SessionType getType() {
        return mType;
    }

    public long getId(long fallback) {
        return mId == null? fallback: mId;
    }

    public long getCreationTime(long fallback) {
        return mCreationTime == null? fallback: mCreationTime;
    }

    public int getTimeout(int fallback) {
        return mSessionTimeout == null? fallback: mSessionTimeout;
    }

    public long getCurrentReportId(long fallback) {
        return mCurrentReportId == null? fallback: mCurrentReportId;
    }

    public boolean isAliveNeeded(boolean fallback) {
        return mAliveNeeded == null? fallback: mAliveNeeded;
    }

    public long getSleepStart(long fallback) {
        return mSleepStart == null? fallback: mSleepStart;
    }

    public long getLastEventOffset(long fallback) {
        return mLastEventOffset == null ? fallback : mLastEventOffset;
    }

    static final class Builder {

        @Nullable
        public Long lastEventOffset;
        @NonNull
        private SessionType type;
        @Nullable
        private Long id;
        @Nullable
        private Long creationTime;
        @Nullable
        private Integer timeout;
        @Nullable
        private Long currentReportId;
        @Nullable
        private Boolean aliveNeeded;
        @Nullable
        private Long sleepStart;

        private Builder(SessionFactoryArguments arguments) {
            this.type = arguments.getType();
            this.timeout = arguments.getSessionTimeout();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCreationTime(Long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder withCurrentReportId(Long currentReportId) {
            this.currentReportId = currentReportId;
            return this;
        }

        public Builder withAliveNeeded(Boolean aliveNeeded) {
            this.aliveNeeded = aliveNeeded;
            return this;
        }

        public Builder withSleepStart(Long sleepStart) {
            this.sleepStart = sleepStart;
            return this;
        }

        public Builder withLastEventOffset(Long lastEventOffset) {
            this.lastEventOffset = lastEventOffset;
            return this;
        }

        public SessionArgumentsInternal build() {
            return new SessionArgumentsInternal(this);
        }

    }
}
