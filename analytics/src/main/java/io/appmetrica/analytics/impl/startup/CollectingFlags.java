package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;

public class CollectingFlags {

    public final boolean permissionsCollectingEnabled;
    public final boolean featuresCollectingEnabled;

    public final boolean googleAid;

    public final boolean simInfo;
    public final boolean huaweiOaid;
    @Nullable
    public final Boolean sslPinning;

    public CollectingFlags(@NonNull CollectingFlagsBuilder builder) {
        this.permissionsCollectingEnabled = builder.mPermissionsCollectingEnabled;
        this.featuresCollectingEnabled = builder.mFeaturesCollectingEnabled;
        this.googleAid = builder.mGoogleAid;
        this.simInfo = builder.mSimInfo;
        this.huaweiOaid = builder.huaweiOaid;
        this.sslPinning = builder.sslPinning;
    }

    @Override
    public String toString() {
        return "CollectingFlags{" +
            "permissionsCollectingEnabled=" + permissionsCollectingEnabled +
            ", featuresCollectingEnabled=" + featuresCollectingEnabled +
            ", googleAid=" + googleAid +
            ", simInfo=" + simInfo +
            ", huaweiOaid=" + huaweiOaid +
            ", sslPinning=" + sslPinning +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectingFlags that = (CollectingFlags) o;

        if (permissionsCollectingEnabled != that.permissionsCollectingEnabled) return false;
        if (featuresCollectingEnabled != that.featuresCollectingEnabled) return false;
        if (googleAid != that.googleAid) return false;
        if (simInfo != that.simInfo) return false;
        if (huaweiOaid != that.huaweiOaid) return false;
        return sslPinning != null ? sslPinning.equals(that.sslPinning) : that.sslPinning == null;
    }

    @Override
    public int hashCode() {
        int result = (permissionsCollectingEnabled ? 1 : 0);
        result = 31 * result + (featuresCollectingEnabled ? 1 : 0);
        result = 31 * result + (googleAid ? 1 : 0);
        result = 31 * result + (simInfo ? 1 : 0);
        result = 31 * result + (huaweiOaid ? 1 : 0);
        result = 31 * result + (sslPinning != null ? sslPinning.hashCode() : 0);
        return result;
    }

    public static final class Constants {

        private static final StartupStateProtobuf.StartupState.Flags DEFAULTS =
                new StartupStateProtobuf.StartupState.Flags();

        public static final boolean PERMISSIONS_COLLECTING_ENABLED = DEFAULTS.permissionsCollectingEnabled;
        public static final boolean FEATURES_COLLECTING_ENABLED = DEFAULTS.featuresCollectingEnabled;
        public static final boolean GOOGLE_AID = DEFAULTS.googleAid;
        public static final boolean SIM_INFO = DEFAULTS.simInfo;
        public static final boolean HUAWEI_OAID = DEFAULTS.huaweiOaid;
    }

    public static class CollectingFlagsBuilder {

        private boolean mPermissionsCollectingEnabled = Constants.PERMISSIONS_COLLECTING_ENABLED;
        private boolean mFeaturesCollectingEnabled = Constants.FEATURES_COLLECTING_ENABLED;
        private boolean mGoogleAid = Constants.GOOGLE_AID;
        private boolean mSimInfo = Constants.SIM_INFO;
        private boolean huaweiOaid = Constants.HUAWEI_OAID;
        @Nullable
        private Boolean sslPinning = null;

        @NonNull
        public CollectingFlagsBuilder withPermissionsCollectingEnabled(boolean permissionsCollectingEnabled) {
            mPermissionsCollectingEnabled = permissionsCollectingEnabled;
            return this;
        }

        @NonNull
        public CollectingFlagsBuilder withFeaturesCollectingEnabled(boolean featuresCollectingEnabled) {
            mFeaturesCollectingEnabled = featuresCollectingEnabled;
            return this;
        }

        @NonNull
        public CollectingFlagsBuilder withGoogleAid(boolean googleAid) {
            mGoogleAid = googleAid;
            return this;
        }

        @NonNull
        public CollectingFlagsBuilder withSimInfo(boolean simInfo) {
            mSimInfo = simInfo;
            return this;
        }

        @NonNull
        public CollectingFlagsBuilder withHuaweiOaid(boolean huaweiOaid) {
            this.huaweiOaid = huaweiOaid;
            return this;
        }

        @NonNull
        public CollectingFlagsBuilder withSslPinning(@Nullable Boolean enabled) {
            this.sslPinning = enabled;
            return this;
        }

        @NonNull
        public CollectingFlags build() {
            return new CollectingFlags(this);
        }
    }
}
