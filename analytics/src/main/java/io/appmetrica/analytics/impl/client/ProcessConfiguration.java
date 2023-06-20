package io.appmetrica.analytics.impl.client;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.ResultReceiver;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.impl.AppMetricaInternalConfigExtractor;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.referrer.common.Constants;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProcessConfiguration implements Parcelable {

    public static final String PROCESS_SESSION_ID = UUID.randomUUID().toString();

    public static final String PROCESS_CONFIG_KEY = "PROCESS_CFG_OBJ";
    private static final String KEY = "CFG_KEY_PROCESS_ENVIRONMENT";
    private static final String RECEIVER_KEY = "CFG_KEY_PROCESS_ENVIRONMENT_RECEIVER";

    public static final Parcelable.Creator<ProcessConfiguration> CREATOR =
            new Parcelable.Creator<ProcessConfiguration>() {

                public ProcessConfiguration createFromParcel(Parcel srcObj) {
                    Bundle data = srcObj.readBundle(DataResultReceiver.class.getClassLoader());
                    ContentValues cv = data.getParcelable(KEY);
                    ResultReceiver receiver = data.getParcelable(RECEIVER_KEY);
                    return new ProcessConfiguration(cv, receiver);
                }

                public ProcessConfiguration[] newArray(int size) {
                    return new ProcessConfiguration[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY, mParamsMapping);
        bundle.putParcelable(RECEIVER_KEY, mDataResultReceiver);
        dest.writeBundle(bundle);
    }

    @Override
    public String toString() {
        return "ProcessConfiguration{" +
                "mParamsMapping=" + mParamsMapping +
                ", mDataResultReceiver=" + mDataResultReceiver +
                '}';
    }

    private interface Keys {
        String CFG_PREFIX = "PROCESS_CFG_";
        String CUSTOM_HOSTS = CFG_PREFIX + "CUSTOM_HOSTS";
        String CLIENT_CLIDS = CFG_PREFIX + "CLIDS";
        String DISTRIBUTION_REFERRER = CFG_PREFIX + "DISTRIBUTION_REFERRER";
        String INSTALL_REFERRER_SOURCE = CFG_PREFIX + "INSTALL_REFERRER_SOURCE";
        String PROCESS_ID = CFG_PREFIX + "PROCESS_ID";
        String PROCESS_SESSION_ID = CFG_PREFIX + "PROCESS_SESSION_ID";
        String SDK_API_LEVEL = CFG_PREFIX + "SDK_API_LEVEL";
        String PACKAGE_NAME = CFG_PREFIX + "PACKAGE_NAME";
    }

    @NonNull private final ContentValues mParamsMapping;
    @Nullable private ResultReceiver mDataResultReceiver;

    @Nullable
    public static ProcessConfiguration fromBundle(final Bundle extras) {
        if (null != extras) {
            try {
                return extras.getParcelable(PROCESS_CONFIG_KEY);
            } catch (Throwable error) {
                return null;
            }
        } else {
            return null;
        }
    }

    public ProcessConfiguration(Context context, @Nullable ResultReceiver receiver) {
        mParamsMapping = new ContentValues();

        mParamsMapping.put(Keys.PROCESS_ID, Process.myPid());
        mParamsMapping.put(Keys.PROCESS_SESSION_ID, PROCESS_SESSION_ID);
        mParamsMapping.put(Keys.SDK_API_LEVEL, AppMetrica.getLibraryApiLevel());
        mParamsMapping.put(Keys.PACKAGE_NAME, context.getPackageName());

        mDataResultReceiver = receiver;
    }

    public ProcessConfiguration(ProcessConfiguration other) {
        synchronized (other) {
            mParamsMapping = new ContentValues(other.mParamsMapping);
            mDataResultReceiver = other.mDataResultReceiver;
        }
    }

    public ProcessConfiguration(@NonNull ContentValues values, @Nullable ResultReceiver receiver) {
        mParamsMapping = values == null ? new ContentValues() : values;
        mDataResultReceiver = receiver;
    }

    public void update(@Nullable AppMetricaConfig config) {
        if (config != null) {
            synchronized (this) {
                applyCustomHostsFromConfig(config);
                applyClidsFromConfig(config);
                applyDistributionReferrerFromConfig(config);
            }
        }
    }

    private void applyCustomHostsFromConfig(@NonNull AppMetricaConfig config) {
        if (config.customHosts != null) {
            setCustomHosts(config.customHosts);
        }
    }

    private void applyClidsFromConfig(@NonNull AppMetricaConfig config) {
        Map<String, String> clids = AppMetricaInternalConfigExtractor.getClids(config);
        if (clids != null) {
            setClientClids(StartupUtils.validateClids(clids));
        }
    }

    private void applyDistributionReferrerFromConfig(@NonNull AppMetricaConfig config) {
        String distributionReferrer = AppMetricaInternalConfigExtractor.getDistributionReferrer(config);
        if (distributionReferrer != null) {
            setDistributionReferrer(distributionReferrer);
            setInstallReferrerSource(Constants.INSTALL_REFERRER_SOURCE_API);
        }
    }

    public boolean hasCustomHosts() {
        return mParamsMapping.containsKey(Keys.CUSTOM_HOSTS);
    }

    @Nullable
    public List<String> getCustomHosts() {
        String stringValue = mParamsMapping.getAsString(Keys.CUSTOM_HOSTS);
        List<String> result = TextUtils.isEmpty(stringValue) ? null : JsonHelper.jsonToList(stringValue);
        return result;
    }

    public synchronized void setCustomHosts(@Nullable final List<String> customHostUrlList) {
        mParamsMapping.put(Keys.CUSTOM_HOSTS, JsonHelper.listToJsonString(customHostUrlList));
    }

    @Nullable
    public Map<String, String> getClientClids() {
        String json = mParamsMapping.getAsString(Keys.CLIENT_CLIDS);
        return JsonHelper.jsonToMap(json);
    }

    public synchronized void setClientClids(@Nullable final Map<String, String> clids) {
        mParamsMapping.put(Keys.CLIENT_CLIDS, JsonHelper.mapToJsonString(clids));
    }

    @Nullable
    public String getDistributionReferrer() {
        return mParamsMapping.getAsString(Keys.DISTRIBUTION_REFERRER);
    }

    public synchronized void setDistributionReferrer(@Nullable String referrer) {
        mParamsMapping.put(Keys.DISTRIBUTION_REFERRER, referrer);
    }

    @Nullable
    public String getInstallReferrerSource() {
        return mParamsMapping.getAsString(Keys.INSTALL_REFERRER_SOURCE);
    }

    public synchronized void setInstallReferrerSource(@Nullable String source) {
        mParamsMapping.put(Keys.INSTALL_REFERRER_SOURCE, source);
    }

    @NonNull
    public Integer getProcessID() {
        return mParamsMapping.getAsInteger(Keys.PROCESS_ID);
    }

    @NonNull
    public String getProcessSessionID() {
        return mParamsMapping.getAsString(Keys.PROCESS_SESSION_ID);
    }

    public int getSdkApiLevel() {
        return mParamsMapping.getAsInteger(Keys.SDK_API_LEVEL);
    }

    @NonNull
    public String getPackageName() {
        return mParamsMapping.getAsString(Keys.PACKAGE_NAME);
    }

    @Nullable
    public ResultReceiver getDataResultReceiver() {
        return mDataResultReceiver;
    }

    public synchronized void toBundle(@NonNull Bundle bundle) {
        bundle.putParcelable(PROCESS_CONFIG_KEY, this);
    }

}
