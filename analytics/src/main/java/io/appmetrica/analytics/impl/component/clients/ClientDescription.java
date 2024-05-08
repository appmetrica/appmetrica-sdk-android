package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.logger.internal.YLogger;

public class ClientDescription {

    private static final String TAG = "[ClientDescription]";

    @Nullable private final String mApiKey;
    @NonNull private final String mPackageName;
    @Nullable private final Integer mProcessID;
    @Nullable private final String mProcessSessionID;

    @NonNull
    private final CounterConfigurationReporterType mReporterType;

    public ClientDescription(@Nullable String apiKey,
                             @NonNull String packageName,
                             @Nullable Integer processID,
                             @Nullable String processSessionID,
                             @NonNull CounterConfigurationReporterType reporterType) {
        mApiKey = apiKey;
        mPackageName = packageName;
        mProcessID = processID;
        mProcessSessionID = processSessionID;
        mReporterType = reporterType;
        YLogger.debug(
            TAG,
            "create ClientDescription for next arguments: " +
                "package %s, apiKey %s, processId %d, psid %s, reporter_type: %s",
            mPackageName,
            mApiKey,
            mProcessID,
            mProcessSessionID,
            mReporterType
        );
    }

    @Nullable
    public String getApiKey() {
        return mApiKey;
    }

    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    @Nullable
    public Integer getProcessID() {
        return mProcessID;
    }

    @Nullable
    public String getProcessSessionID() {
        return mProcessSessionID;
    }

    @NonNull
    public CounterConfigurationReporterType getReporterType() {
        return mReporterType;
    }

    @NonNull
    public static ClientDescription fromClientConfiguration(@NonNull ClientConfiguration configuration) {
        return new ClientDescription(
                configuration.getReporterConfiguration().getApiKey(),
                configuration.getProcessConfiguration().getPackageName(),
                configuration.getProcessConfiguration().getProcessID(),
                configuration.getProcessConfiguration().getProcessSessionID(),
                configuration.getReporterConfiguration().getReporterType()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientDescription that = (ClientDescription) o;

        if (mApiKey != null ? !mApiKey.equals(that.mApiKey) : that.mApiKey != null) return false;
        if (!mPackageName.equals(that.mPackageName)) return false;
        if (mProcessID != null ? !mProcessID.equals(that.mProcessID) : that.mProcessID != null)
            return false;
        if (mProcessSessionID != null ? !mProcessSessionID.equals(that.mProcessSessionID) :
                that.mProcessSessionID != null)
            return false;
        return mReporterType == that.mReporterType;
    }

    @Override
    public int hashCode() {
        int result = mApiKey != null ? mApiKey.hashCode() : 0;
        result = 31 * result + mPackageName.hashCode();
        result = 31 * result + (mProcessID != null ? mProcessID.hashCode() : 0);
        result = 31 * result + (mProcessSessionID != null ? mProcessSessionID.hashCode() : 0);
        result = 31 * result + mReporterType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClientDescription{" +
                "mApiKey='" + mApiKey + '\'' +
                ", mPackageName='" + mPackageName + '\'' +
                ", mProcessID=" + mProcessID +
                ", mProcessSessionID='" + mProcessSessionID + '\'' +
                ", mReporterType=" + mReporterType +
                '}';
    }
}
