package io.appmetrica.analytics.internal;

import android.content.ContentValues;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.Collections;
import java.util.List;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreutils.internal.LocationUtils;
import io.appmetrica.analytics.impl.CounterConfigurationKeys;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.Utils;

@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class CounterConfiguration implements Parcelable {

    private static final String DATA = "io.appmetrica.analytics.internal.CounterConfiguration.data";

    @Override
    public synchronized String toString() {
        return "CounterConfiguration{" +
                "mParamsMapping=" + mParamsMapping +
                '}';
    }

    private final ContentValues mParamsMapping;

    public CounterConfiguration(@NonNull CounterConfiguration other) {
        synchronized (other) {
            mParamsMapping = new ContentValues(other.mParamsMapping);
        }
    }

    public CounterConfiguration() {
        mParamsMapping = new ContentValues();
    }

    public CounterConfiguration(@NonNull String apiKey) {
        this();
        synchronized (this) {
            setApiKey(apiKey);
        }
    }

    public CounterConfiguration(@NonNull CounterConfigurationReporterType reporterType) {
        this();
        synchronized (this) {
            setReporterType(reporterType);
        }
    }

    public CounterConfiguration(
            AppMetricaConfig config,
            @NonNull CounterConfigurationReporterType reporterType
    ) {
        this();
        synchronized (this) {
            applyFromConfig(config);
            setReporterType(reporterType);
        }
    }

    public CounterConfiguration(@NonNull ReporterConfig config) {
        this();
        synchronized (this) {
            applyApiKeyFromConfig(config.apiKey);
            applySessionTimeoutFromConfig(config.sessionTimeout);
            applyDispatchPeriodFromConfig(config.dispatchPeriodSeconds);
            applyMaxReportsCountFromConfig(config.maxReportsCount);
            applyLogsFromConfig(config.logs);
            applyDataSendingEnabledFromConfig(config.dataSendingEnabled);
            applyMaxReportsInDbCount(config.maxReportsInDatabaseCount);
            setManualReporterType(config.apiKey);
        }
    }

    public synchronized void applyFromConfig(@NonNull AppMetricaConfig config) {
        applyApiKeyFromConfig(config.apiKey);
        applySessionTimeoutFromConfig(config.sessionTimeout);
        applyManualLocationFromConfig(config);
        applyLocationTrackingFromConfig(config);
        applyAdvIdentifiersTrackingFromConfig(config);
        applyDeviceTypeFromConfig(config);
        applyDispatchPeriodFromConfig(config.dispatchPeriodSeconds);
        applyMaxReportsCountFromConfig(config.maxReportsCount);
        applyLogsFromConfig(config.logs);
        applyAppVersionFromConfig(config);
        applyAppBuildNumberFromConfig(config);
        applyFirstActivationAsUpdateFromConfig(config);
        applyDataSendingEnabledFromConfig(config.dataSendingEnabled);
        applyMaxReportsInDbCount(config.maxReportsInDatabaseCount);
        applyNativeCrashesFromConfig(config.nativeCrashReporting);
        applyRevenueAutoTrackingEnabledFromConfig(config);
    }

    private void setManualReporterType(@Nullable String apiKey) {
        if (SdkData.SDK_API_KEY_UUID.equals(apiKey)) {
            setReporterType(CounterConfigurationReporterType.SELF_SDK);
        } else {
            setReporterType(CounterConfigurationReporterType.MANUAL);
        }
    }

    private void applyApiKeyFromConfig(@Nullable String apiKey) {
        if (Utils.isFieldSet(apiKey)) {
            setApiKey(apiKey);
        }
    }

    private void applySessionTimeoutFromConfig(@Nullable Integer sessionTimeout) {
        if (Utils.isFieldSet(sessionTimeout)) {
            setSessionTimeout(sessionTimeout);
        }
    }

    private void applyManualLocationFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.location)) {
            setManualLocation(config.location);
        }
    }

    private void applyLocationTrackingFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.locationTracking)) {
            setLocationTracking(config.locationTracking);
        }
    }

    private void applyAdvIdentifiersTrackingFromConfig(@NonNull AppMetricaConfig config) {
        if (Utils.isFieldSet(config.advIdentifiersTracking)) {
            setAdvIdentifiersTracking(config.advIdentifiersTracking);
        }
    }

    private void applyRevenueAutoTrackingEnabledFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.revenueAutoTrackingEnabled)) {
            setRevenueAutoTrackingEnabled(config.revenueAutoTrackingEnabled);
        }
    }

    private void applyDeviceTypeFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.deviceType)) {
            setDeviceType(config.deviceType);
        }
    }

    private void applyDispatchPeriodFromConfig(@Nullable Integer dispatchPeriodSeconds) {
        if (Utils.isFieldSet(dispatchPeriodSeconds)) {
            setDispatchPeriod(dispatchPeriodSeconds);
        }
    }

    private void applyMaxReportsCountFromConfig(@Nullable Integer maxReportsCount) {
        if (Utils.isFieldSet(maxReportsCount)) {
            setMaxReportsCount(maxReportsCount);
        }
    }

    private void applyLogsFromConfig(@Nullable Boolean logs) {
        if (Utils.isFieldSet(logs)) {
            setLogEnabled(logs);
        }
    }

    private void applyAppVersionFromConfig(AppMetricaConfig config) {
        String appVersion = config.appVersion;
        if (!TextUtils.isEmpty(appVersion)) {
            setCustomAppVersion(config.appVersion);
        }
    }

    private void applyAppBuildNumberFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.appBuildNumber)) {
            setAppBuildNumber(config.appBuildNumber);
        }
    }

    private void applyFirstActivationAsUpdateFromConfig(AppMetricaConfig config) {
        if (Utils.isFieldSet(config.firstActivationAsUpdate)) {
            setFirstActivationAsUpdate(config.firstActivationAsUpdate);
        }
    }

    private void applyDataSendingEnabledFromConfig(@Nullable Boolean dataSendingEnabled) {
        if (Utils.isFieldSet(dataSendingEnabled)) {
            setDataSendingEnabled(dataSendingEnabled);
        }
    }

    private void applyMaxReportsInDbCount(@Nullable Integer maxReportsInDbCount) {
        if (Utils.isFieldSet(maxReportsInDbCount)) {
            mParamsMapping.put(CounterConfigurationKeys.MAX_REPORTS_IN_DB_COUNT, maxReportsInDbCount);
        }
    }

    private void applyNativeCrashesFromConfig(@Nullable Boolean nativeCrashes) {
        if (Utils.isFieldSet(nativeCrashes)) {
            mParamsMapping.put(CounterConfigurationKeys.NATIVE_CRASHES_ENABLED, nativeCrashes);
        }
    }

    @VisibleForTesting
    public synchronized void setDispatchPeriod(final int dispatchPeriodSeconds) {
        mParamsMapping.put(CounterConfigurationKeys.DISPATCH_PERIOD, dispatchPeriodSeconds);
    }

    @Nullable
    public Integer getDispatchPeriod() {
        return mParamsMapping.getAsInteger(CounterConfigurationKeys.DISPATCH_PERIOD);
    }

    @VisibleForTesting
    public synchronized void setMaxReportsCount(final int maxReportsCount) {
        mParamsMapping.put(
                CounterConfigurationKeys.MAX_REPORTS_COUNT,
                (maxReportsCount <= 0 ? Integer.MAX_VALUE : maxReportsCount)
        );
    }

    @Nullable
    public Integer getMaxReportsCount() {
        return mParamsMapping.getAsInteger(CounterConfigurationKeys.MAX_REPORTS_COUNT);
    }

    @VisibleForTesting
    public synchronized void setSessionTimeout(final int sessionTimeout) {
        mParamsMapping.put(CounterConfigurationKeys.SESSION_TIMEOUT, sessionTimeout);
    }

    @Nullable
    public Integer getSessionTimeout() {
        return mParamsMapping.getAsInteger(CounterConfigurationKeys.SESSION_TIMEOUT);
    }

    public final synchronized void setDeviceType(@Nullable String deviceType) {
        mParamsMapping.put(
                CounterConfigurationKeys.DEVICE_SIZE_TYPE,
                TextUtils.isEmpty(deviceType) ? null : deviceType
        );
    }

    @Nullable
    public String getDeviceType() {
        return mParamsMapping.getAsString(CounterConfigurationKeys.DEVICE_SIZE_TYPE);
    }

    @VisibleForTesting
    public synchronized void setApiKey(final String apiKey) {
        mParamsMapping.put(CounterConfigurationKeys.API_KEY, apiKey);
    }

    public synchronized void setUuid(final String uuid) {
        mParamsMapping.put(CounterConfigurationKeys.UUID, uuid);
    }

    public String getUuid() {
        return mParamsMapping.getAsString(CounterConfigurationKeys.UUID);
    }

    public String getApiKey() {
        return mParamsMapping.getAsString(CounterConfigurationKeys.API_KEY);
    }

    public synchronized void setLocationTracking(final boolean enabled) {
        mParamsMapping.put(CounterConfigurationKeys.LOCATION_TRACKING_ENABLED, enabled);
    }

    public synchronized void setAdvIdentifiersTracking(final boolean enabled) {
        mParamsMapping.put(CounterConfigurationKeys.ADV_IDENTIFIERS_TRACKING_ENABLED, enabled);
    }

    public synchronized void setRevenueAutoTrackingEnabled(final boolean enabled) {
        mParamsMapping.put(CounterConfigurationKeys.REVENUE_AUTO_TRACKING_ENABLED, enabled);
    }

    @Nullable
    public synchronized Boolean isRevenueAutoTrackingEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.REVENUE_AUTO_TRACKING_ENABLED);
    }

    @Nullable
    public Boolean isLocationTrackingEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.LOCATION_TRACKING_ENABLED);
    }

    @Nullable
    public Boolean isAdvIdentifiersTrackingEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.ADV_IDENTIFIERS_TRACKING_ENABLED);
    }

    public final synchronized void setCustomAppVersion(final String appVersion) {
        mParamsMapping.put(CounterConfigurationKeys.APP_VERSION, appVersion);
    }

    public String getAppVersion() {
        return mParamsMapping.getAsString(CounterConfigurationKeys.APP_VERSION);
    }

    public synchronized void setAppBuildNumber(final int appBuildNumber) {
        mParamsMapping.put(CounterConfigurationKeys.APP_BUILD_NUMBER, String.valueOf(appBuildNumber));
    }

    public String getAppBuildNumber() {
        return mParamsMapping.getAsString(CounterConfigurationKeys.APP_BUILD_NUMBER);
    }

    public final synchronized void setManualLocation(@Nullable final Location location) {
        mParamsMapping.put(CounterConfigurationKeys.MANUAL_LOCATION, LocationUtils.locationToBytes(location));
    }

    public synchronized void setLogEnabled(final boolean enabled) {
        mParamsMapping.put(CounterConfigurationKeys.IS_LOG_ENABLED, enabled);
    }

    @Nullable
    public Boolean isLogEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.IS_LOG_ENABLED);
    }

    public Location getManualLocation() {
        return mParamsMapping.containsKey(CounterConfigurationKeys.MANUAL_LOCATION) ?
                LocationUtils.bytesToLocation(mParamsMapping.getAsByteArray(CounterConfigurationKeys.MANUAL_LOCATION)) :
                null;
    }

    @Nullable
    public Boolean getReportNativeCrashesEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.NATIVE_CRASHES_ENABLED);
    }

    public final synchronized void setFirstActivationAsUpdate(final boolean value) {
        mParamsMapping.put(CounterConfigurationKeys.IS_FIRST_ACTIVATION_AS_UPDATE, value);
    }

    @Nullable
    public Boolean isFirstActivationAsUpdate() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.IS_FIRST_ACTIVATION_AS_UPDATE);
    }

    @VisibleForTesting
    public void setMaxReportsInDbCount(final int maxReportsInDbCount) {
        mParamsMapping.put(CounterConfigurationKeys.MAX_REPORTS_IN_DB_COUNT, maxReportsInDbCount);
    }

    @Nullable
    public Integer getMaxReportsInDbCount() {
        return mParamsMapping.getAsInteger(CounterConfigurationKeys.MAX_REPORTS_IN_DB_COUNT);
    }

    public Boolean getDataSendingEnabled() {
        return mParamsMapping.getAsBoolean(CounterConfigurationKeys.DATA_SENDING_ENABLED);
    }

    public final synchronized void setDataSendingEnabled(boolean value) {
        mParamsMapping.put(CounterConfigurationKeys.DATA_SENDING_ENABLED, value);
    }

    public final synchronized void addAutoCollectedDataSubscriber(@NonNull final String subscriber) {
        addAutoCollectedDataSubscribers(Collections.singletonList(subscriber));
    }

    public final synchronized void addAutoCollectedDataSubscribers(@NonNull List<String> subscribers) {
        List<String> actualSubscribers = getAutoCollectedDataSubscribers();
        actualSubscribers.addAll(subscribers);
        Utils.writeStringArrayListToContentValues(
            mParamsMapping,
            CounterConfigurationKeys.AUTO_COLLECTED_DATA_SUBSCRIBERS,
            subscribers
        );
    }

    @NonNull
    public List<String> getAutoCollectedDataSubscribers() {
        return Utils.readStringArrayListFromContentValues(
            mParamsMapping,
            CounterConfigurationKeys.AUTO_COLLECTED_DATA_SUBSCRIBERS
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public synchronized void writeToParcel(Parcel destObj, int flags) {
        Bundle data = new Bundle();
        data.putParcelable(DATA, mParamsMapping);
        destObj.writeBundle(data);
    }

    public static final Parcelable.Creator<CounterConfiguration> CREATOR =
            new Parcelable.Creator<CounterConfiguration>() {

                public CounterConfiguration createFromParcel(Parcel srcObj) {
                    Bundle data = srcObj.readBundle(DataResultReceiver.class.getClassLoader());
                    ContentValues cv = data.getParcelable(DATA);
                    return new CounterConfiguration(cv);
                }

                public CounterConfiguration[] newArray(int size) {
                    return new CounterConfiguration[size];
                }

            };

    public synchronized void setReporterType(@NonNull CounterConfigurationReporterType reporterType) {
        mParamsMapping.put(CounterConfigurationKeys.REPORTER_TYPE, reporterType.getStringValue());
    }

    @NonNull
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.fromStringValue(
                mParamsMapping.getAsString(CounterConfigurationKeys.REPORTER_TYPE)
        );
    }

    public synchronized void toBundle(Bundle result) {
        result.putParcelable(CounterConfigurationKeys.METRICA_CONFIG_EXTRA, this);
    }

    public static CounterConfiguration fromBundle(final Bundle extras) {
        CounterConfiguration sdkConfig = null;

        if (null != extras) {
            try {
                sdkConfig = extras.getParcelable(CounterConfigurationKeys.METRICA_CONFIG_EXTRA);
            } catch (Throwable error) {
                return null;
            }
        }

        return sdkConfig;
    }

    private CounterConfiguration(ContentValues cv) {
        mParamsMapping = cv;
    }
}
