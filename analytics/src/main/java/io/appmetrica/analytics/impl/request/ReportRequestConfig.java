package io.appmetrica.analytics.impl.request;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.startup.ClidsStateChecker;
import io.appmetrica.analytics.impl.startup.StartupState;
import java.util.List;
import java.util.Map;

public class ReportRequestConfig extends CoreRequestConfig {

    @Override
    public String toString() {
        return "ReportRequestConfig{" +
            "mLocationTracking=" + mLocationTracking +
            ", mManualLocation=" + mManualLocation +
            ", mFirstActivationAsUpdate=" + mFirstActivationAsUpdate +
            ", mSessionTimeout=" + mSessionTimeout +
            ", mDispatchPeriod=" + mDispatchPeriod +
            ", mLogEnabled=" + mLogEnabled +
            ", mMaxReportsCount=" + mMaxReportsCount +
            ", dataSendingEnabledFromArguments=" + dataSendingEnabledFromArguments +
            ", dataSendingStrategy=" + dataSendingStrategy +
            ", mPreloadInfoSendingStrategy=" + mPreloadInfoSendingStrategy +
            ", mApiKey='" + mApiKey + '\'' +
            ", mPermissionsCollectingEnabled=" + mPermissionsCollectingEnabled +
            ", mFeaturesCollectingEnabled=" + mFeaturesCollectingEnabled +
            ", mClidsFromStartupResponse='" + mClidsFromStartupResponse + '\'' +
            ", mReportHosts=" + mReportHosts +
            ", mAttributionId=" + mAttributionId +
            ", mPermissionsCollectingIntervalSeconds=" + mPermissionsCollectingIntervalSeconds +
            ", mPermissionsForceSendIntervalSeconds=" + mPermissionsForceSendIntervalSeconds +
            ", mClidsFromClientMatchClidsFromStartupRequest=" + mClidsFromClientMatchClidsFromStartupRequest +
            ", mMaxReportsInDbCount=" + mMaxReportsInDbCount +
            ", mCertificates=" + mCertificates +
            "} " + super.toString();
    }

    public interface DataSendingStrategy {

        boolean shouldSend(@Nullable Boolean fromArguments);

    }

    public interface PreloadInfoSendingStrategy {

        boolean shouldSend();

    }

    public abstract static class BaseDataSendingStrategy implements DataSendingStrategy {

        @NonNull
        protected final DataSendingRestrictionController controller;

        public BaseDataSendingStrategy(@NonNull DataSendingRestrictionController controller) {
            this.controller = controller;
        }

        @Override
        public boolean shouldSend(@Nullable Boolean fromArguments) {
            return WrapUtils.getOrDefault(
                fromArguments,
                AppMetricaDefaultValues.DEFAULT_REPORTER_DATA_SENDING_ENABLED
            );
        }
    }

    private boolean mLocationTracking;
    private Location mManualLocation;
    private boolean mFirstActivationAsUpdate;
    private int mSessionTimeout;
    private int mDispatchPeriod;
    private boolean mLogEnabled;
    private int mMaxReportsCount;
    private Boolean dataSendingEnabledFromArguments;
    private DataSendingStrategy dataSendingStrategy;
    @NonNull
    private final PreloadInfoSendingStrategy mPreloadInfoSendingStrategy;

    // AppMetricaDeviceIdentifiers
    private String mApiKey;

    private boolean mPermissionsCollectingEnabled;
    private boolean mFeaturesCollectingEnabled;

    private String mClidsFromStartupResponse;

    private List<String> mReportHosts;

    private int mAttributionId;
    private long mPermissionsCollectingIntervalSeconds;
    private long mPermissionsForceSendIntervalSeconds;
    private boolean mClidsFromClientMatchClidsFromStartupRequest;
    private long mMaxReportsInDbCount;

    @Nullable
    private List<String> mCertificates;

    @VisibleForTesting()
    ReportRequestConfig(@NonNull PreloadInfoSendingStrategy preloadInfoSendingStrategy) {
        mPreloadInfoSendingStrategy = preloadInfoSendingStrategy;
    }

    @NonNull
    public String getClidsFromStartupResponse() {
        return WrapUtils.getOrDefault(mClidsFromStartupResponse, StringUtils.EMPTY);
    }

    void setClidsFromStartupResponse(final String clids) {
        mClidsFromStartupResponse = clids;
    }

    public void setReportHosts(final List<String> hosts) {
        mReportHosts = hosts;
    }

    public List<String> getReportHosts() {
        return mReportHosts;
    }

    public String getApiKey() {
        return mApiKey;
    }

    public boolean isPermissionsCollectingEnabled() {
        return mPermissionsCollectingEnabled;
    }

    public boolean isFeaturesCollectingEnabled() {
        return mFeaturesCollectingEnabled;
    }

    public void setPermissionsCollectingIntervalSeconds(long permissionsCollectingIntervalSeconds) {
        mPermissionsCollectingIntervalSeconds = permissionsCollectingIntervalSeconds;
    }

    public long getPermissionsCollectingIntervalSeconds() {
        return mPermissionsCollectingIntervalSeconds;
    }

    public void setPermissionsForceSendIntervalSeconds(long permissionsForceSendIntervalSeconds) {
        mPermissionsForceSendIntervalSeconds = permissionsForceSendIntervalSeconds;
    }

    public long getPermissionsForceSendIntervalSeconds() {
        return mPermissionsForceSendIntervalSeconds;
    }

    public void setPermissionsCollectingEnabled(boolean value) {
        mPermissionsCollectingEnabled = value;
    }

    public void setFeaturesCollectingEnabled(boolean value) {
        mFeaturesCollectingEnabled = value;
    }

    public boolean isReadyForSending() {
        return isIdentifiersValid() &&
            Utils.isNullOrEmpty(getReportHosts()) == false &&
            getClidsFromClientMatchClidsFromStartupRequest();
    }

    private void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    public boolean needToSendPreloadInfo() {
        return mPreloadInfoSendingStrategy.shouldSend();
    }

    public boolean isLocationTracking() {
        return mLocationTracking;
    }

    public void setLocationTracking(boolean locationTracking) {
        mLocationTracking = locationTracking;
    }

    public Location getManualLocation() {
        return mManualLocation;
    }

    public void setManualLocation(Location manualLocation) {
        mManualLocation = manualLocation;
    }

    public boolean isFirstActivationAsUpdate() {
        return mFirstActivationAsUpdate;
    }

    public void setFirstActivationAsUpdate(boolean firstActivationAsUpdate) {
        mFirstActivationAsUpdate = firstActivationAsUpdate;
    }

    public int getSessionTimeout() {
        return mSessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        mSessionTimeout = sessionTimeout;
    }

    public int getDispatchPeriod() {
        return mDispatchPeriod;
    }

    public void setDispatchPeriod(int dispatchPeriod) {
        mDispatchPeriod = dispatchPeriod;
    }

    public boolean isLogEnabled() {
        return mLogEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        mLogEnabled = logEnabled;
    }

    public int getMaxReportsCount() {
        return mMaxReportsCount;
    }

    public void setMaxReportsCount(int maxReportsCount) {
        mMaxReportsCount = maxReportsCount;
    }

    public int getAttributionId() {
        return mAttributionId;
    }

    public void setAttributionId(final int attributionId) {
        mAttributionId = attributionId;
    }

    public long getMaxEventsInDbCount() {
        return mMaxReportsInDbCount;
    }

    public void setMaxReportsInDbCount(long maxReportsInDbCount) {
        mMaxReportsInDbCount = maxReportsInDbCount;
    }

    public boolean getCurrentDataSendingState() {
        return dataSendingStrategy.shouldSend(dataSendingEnabledFromArguments);
    }

    @Nullable
    public List<String> getCertificates() {
        return mCertificates;
    }

    public void setCertificates(@NonNull List<String> certificates) {
        mCertificates = certificates;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    DataSendingStrategy getDataSendingStrategy() {
        return dataSendingStrategy;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    PreloadInfoSendingStrategy getPreloadInfoSendingStrategy() {
        return mPreloadInfoSendingStrategy;
    }

    public void setDataSendingProperties(@Nullable Boolean dataSendingFromArguments,
                                         @NonNull DataSendingStrategy dataSendingStrategy) {
        this.dataSendingEnabledFromArguments = dataSendingFromArguments;
        this.dataSendingStrategy = dataSendingStrategy;
    }

    public boolean getClidsFromClientMatchClidsFromStartupRequest() {
        return mClidsFromClientMatchClidsFromStartupRequest;
    }

    public void setClidsFromClientMatchClidsFromStartupRequest(final boolean match) {
        mClidsFromClientMatchClidsFromStartupRequest = match;
    }

    public static final class Arguments extends BaseRequestArguments<CommonArguments.ReporterArguments, Arguments> {

        @SuppressLint("NewApi")
        public int locationHashCode(@NonNull Location location) {
            int result;
            long temp;
            result = location.getProvider() != null ? location.getProvider().hashCode() : 0;
            result = 31 * result + (int) (location.getTime() ^ (location.getTime() >>> 32));

            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                result = 31 * result +
                    (int) (location.getElapsedRealtimeNanos() ^ (location.getElapsedRealtimeNanos() >>> 32));
            }
            temp = Double.doubleToLongBits(location.getLatitude());
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(location.getLongitude());
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(location.getAltitude());
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result
                + (location.getSpeed() != +0.0f ? Float.floatToIntBits(location.getSpeed()) : 0);
            result = 31 * result
                + (location.getBearing() != +0.0f ? Float.floatToIntBits(location.getBearing()) : 0);
            result = 31 * result
                + (location.getAccuracy() != +0.0f ? Float.floatToIntBits(location.getAccuracy()) : 0);
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.O)) {
                result = 31 * result + (location.getVerticalAccuracyMeters() != +0.0f ?
                    Float.floatToIntBits(location.getVerticalAccuracyMeters()) : 0
                );
                result = 31 * result + (location.getSpeedAccuracyMetersPerSecond() != +0.0f ?
                    Float.floatToIntBits(location.getSpeedAccuracyMetersPerSecond()) : 0
                );
                result = 31 * result + (location.getBearingAccuracyDegrees() != +0.0f ?
                    Float.floatToIntBits(location.getBearingAccuracyDegrees()) : 0
                );
            }
            result = 31 * result + (location.getExtras() != null ? location.getExtras().hashCode() : 0);
            return result;
        }

        @SuppressLint("NewApi")
        boolean compareLocations(@Nullable Location thisLocation, @Nullable Location thatLocation) {
            if (thisLocation == thatLocation) {
                return true;
            }
            if (thisLocation == null || thatLocation == null) {
                return false;
            }

            if (thisLocation.getTime() != thatLocation.getTime()) return false;
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                if (thisLocation.getElapsedRealtimeNanos() != thatLocation.getElapsedRealtimeNanos()) return false;
            }
            if (Double.compare(thatLocation.getLatitude(), thisLocation.getLatitude()) != 0) return false;
            if (Double.compare(thatLocation.getLongitude(), thisLocation.getLongitude()) != 0) return false;
            if (Double.compare(thatLocation.getAltitude(), thisLocation.getAltitude()) != 0) return false;
            if (Float.compare(thatLocation.getSpeed(), thisLocation.getSpeed()) != 0) return false;
            if (Float.compare(thatLocation.getBearing(), thisLocation.getBearing()) != 0) return false;
            if (Float.compare(thatLocation.getAccuracy(), thisLocation.getAccuracy()) != 0) return false;
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.O)) {
                if (Float.compare(thatLocation.getVerticalAccuracyMeters(),
                    thisLocation.getVerticalAccuracyMeters()) != 0) {
                    return false;
                }
                if (Float.compare(thatLocation.getSpeedAccuracyMetersPerSecond(),
                    thisLocation.getSpeedAccuracyMetersPerSecond()) != 0) {
                    return false;
                }
                if (Float.compare(thatLocation.getBearingAccuracyDegrees(),
                    thisLocation.getBearingAccuracyDegrees()) != 0) {
                    return false;
                }
            }
            if (thisLocation.getProvider() != null ?
                !thisLocation.getProvider().equals(thatLocation.getProvider()) :
                thatLocation.getProvider() != null) return false;
            return thisLocation.getExtras() != null ?
                thisLocation.getExtras().equals(thatLocation.getExtras()) :
                thatLocation.getExtras() == null;
        }

        @Nullable
        public final String apiKey;
        @Nullable
        public final Location manualLocation;
        public final boolean locationTracking;
        public final boolean firstActivationAsUpdate;
        public final int sessionTimeout;
        public final int maxReportsCount;
        public final int dispatchPeriod;
        public final boolean logEnabled;
        public final boolean dataSendingEnabled;
        @Nullable
        public final Map<String, String> clidsFromClient;
        public final int maxReportsInDbCount;

        public Arguments(
            @NonNull CommonArguments.ReporterArguments reporterArguments
        ) {
            this(
                reporterArguments.deviceType,
                reporterArguments.appVersion,
                reporterArguments.appBuildNumber,
                reporterArguments.apiKey,
                reporterArguments.locationTracking,
                reporterArguments.manualLocation,
                reporterArguments.firstActivationAsUpdate,
                reporterArguments.sessionTimeout,
                reporterArguments.maxReportsCount,
                reporterArguments.dispatchPeriod,
                reporterArguments.logEnabled,
                reporterArguments.dataSendingEnabled,
                reporterArguments.clidsFromClient,
                reporterArguments.maxReportsInDbCount
            );
        }

        Arguments(@Nullable String deviceType,
                  @Nullable String appVersion,
                  @Nullable String appBuildNumber,
                  @Nullable String apiKey,
                  @Nullable Boolean locationTracking,
                  @Nullable Location manualLocation,
                  @Nullable Boolean firstActivationAsUpdate,
                  @Nullable Integer sessionTimeout,
                  @Nullable Integer maxReportsCount,
                  @Nullable Integer dispatchPeriod,
                  @Nullable Boolean logEnabled,
                  @Nullable Boolean dataSendingEnabled,
                  @Nullable Map<String, String> clidsFromClient,
                  @Nullable Integer maxReportsInDbCount) {
            super(deviceType, appVersion, appBuildNumber);
            this.apiKey = apiKey;
            this.locationTracking = WrapUtils.getOrDefault(locationTracking,
                DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED);
            this.manualLocation = manualLocation;
            this.firstActivationAsUpdate = WrapUtils.getOrDefault(firstActivationAsUpdate,
                DefaultValues.DEFAULT_FIRST_ACTIVATION_AS_UPDATE);
            this.sessionTimeout = Math.max(DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS,
                WrapUtils.getOrDefault(sessionTimeout, DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS)
            );
            this.maxReportsCount = WrapUtils.getOrDefault(maxReportsCount, DefaultValues.DEFAULT_MAX_REPORTS_COUNT);
            this.dispatchPeriod = WrapUtils.getOrDefault(dispatchPeriod,
                DefaultValues.DEFAULT_DISPATCH_PERIOD_SECONDS);
            this.logEnabled = WrapUtils.getOrDefault(logEnabled, DefaultValues.DEFAULT_LOG_ENABLED);
            this.dataSendingEnabled = WrapUtils.getOrDefault(dataSendingEnabled,
                DefaultValues.DEFAULT_DATA_SENDING_ENABLED);
            this.clidsFromClient = clidsFromClient;
            this.maxReportsInDbCount = WrapUtils.getOrDefault(maxReportsInDbCount,
                DefaultValues.MAX_REPORTS_IN_DB_COUNT_DEFAULT);
        }

        public static Arguments empty() {
            return new Arguments(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }

        @Override
        @NonNull
        public Arguments mergeFrom(@NonNull CommonArguments.ReporterArguments other) {
            return new Arguments(
                WrapUtils.getOrDefaultNullable(other.deviceType, deviceType),
                WrapUtils.getOrDefaultNullable(other.appVersion, appVersion),
                WrapUtils.getOrDefaultNullable(
                    other.appBuildNumber, appBuildNumber),
                WrapUtils.getOrDefaultNullable(other.apiKey, apiKey),
                WrapUtils.getOrDefaultNullable(
                    other.locationTracking, locationTracking),
                WrapUtils.getOrDefaultNullable(
                    other.manualLocation, manualLocation),
                WrapUtils.getOrDefaultNullable(
                    other.firstActivationAsUpdate, firstActivationAsUpdate),
                WrapUtils.getOrDefaultNullable(
                    other.sessionTimeout, sessionTimeout),
                WrapUtils.getOrDefaultNullable(
                    other.maxReportsCount, maxReportsCount),
                WrapUtils.getOrDefaultNullable(
                    other.dispatchPeriod, dispatchPeriod),
                WrapUtils.getOrDefaultNullable(other.logEnabled, logEnabled),
                WrapUtils.getOrDefaultNullable(other.dataSendingEnabled, dataSendingEnabled),
                WrapUtils.getOrDefaultNullable(other.clidsFromClient, clidsFromClient),
                WrapUtils.getOrDefaultNullable(other.maxReportsInDbCount, maxReportsInDbCount)
            );
        }

        @SuppressWarnings("checkstyle:methodLength")
        @Override
        public boolean compareWithOtherArguments(@NonNull CommonArguments.ReporterArguments arguments) {
            if (arguments.deviceType != null) {
                if (!arguments.deviceType.equals(deviceType)) {
                    return false;
                }
            }
            if (arguments.appVersion != null) {
                if (!arguments.appVersion.equals(appVersion)) {
                    return false;
                }
            }
            if (arguments.appBuildNumber != null) {
                if (!arguments.appBuildNumber.equals(appBuildNumber)) {
                    return false;
                }
            }
            if (arguments.locationTracking != null) {
                if (locationTracking != arguments.locationTracking) {
                    return false;
                }
            }
            if (arguments.firstActivationAsUpdate != null) {
                if (firstActivationAsUpdate != arguments.firstActivationAsUpdate) {
                    return false;
                }
            }
            if (arguments.sessionTimeout != null) {
                if (sessionTimeout != arguments.sessionTimeout) {
                    return false;
                }
            }
            if (arguments.maxReportsCount != null) {
                if (maxReportsCount != arguments.maxReportsCount) {
                    return false;
                }
            }
            if (arguments.dispatchPeriod != null) {
                if (dispatchPeriod != arguments.dispatchPeriod) {
                    return false;
                }
            }
            if (arguments.logEnabled != null) {
                if (logEnabled != arguments.logEnabled) {
                    return false;
                }
            }
            if (arguments.dataSendingEnabled != null) {
                if (dataSendingEnabled != arguments.dataSendingEnabled) {
                    return false;
                }
            }
            if (arguments.apiKey != null) {
                if (apiKey == null || !apiKey.equals(arguments.apiKey)) {
                    return false;
                }
            }
            if (arguments.clidsFromClient != null) {
                if (clidsFromClient == null || clidsFromClient.equals(arguments.clidsFromClient) == false) {
                    return false;
                }
            }
            if (arguments.maxReportsInDbCount != null) {
                if (maxReportsInDbCount != arguments.maxReportsInDbCount) {
                    return false;
                }
            }
            return arguments.manualLocation == null || compareLocations(manualLocation, arguments.manualLocation);
        }

    }

    public static class Loader extends CoreLoader<ReportRequestConfig, Arguments> {

        @NonNull
        private final ComponentUnit mComponentUnit;
        @NonNull
        private final DataSendingStrategy dataSendingStrategy;
        @NonNull
        private final ClidsStateChecker clidsStateChecker;

        public Loader(@NonNull ComponentUnit componentUnit,
                      @NonNull DataSendingStrategy dataSendingStrategy
        ) {
            this(componentUnit, dataSendingStrategy, new ClidsStateChecker());
        }

        @VisibleForTesting
        Loader(@NonNull ComponentUnit componentUnit,
               @NonNull DataSendingStrategy dataSendingStrategy,
               @NonNull ClidsStateChecker clidsStateChecker) {
            super(componentUnit.getContext(), componentUnit.getComponentId().getPackage());
            mComponentUnit = componentUnit;
            this.dataSendingStrategy = dataSendingStrategy;
            this.clidsStateChecker = clidsStateChecker;
        }

        @NonNull
        @Override
        protected ReportRequestConfig createBlankConfig() {
            return new ReportRequestConfig(mComponentUnit);
        }

        @NonNull
        public ReportRequestConfig load(@NonNull CoreDataSource<Arguments> dataSource) {
            ReportRequestConfig config = super.load(dataSource);

            // Load apiKey from config or manifest
            config.setApiKey(dataSource.componentArguments.apiKey);
            config.setAttributionId(mComponentUnit.getVitalComponentDataProvider().getAttributionId());
            config.setCertificates(mComponentUnit.getCertificatesFingerprintsProvider().getSha1());

            config.setLocationTracking(dataSource.componentArguments.locationTracking);
            config.setManualLocation(dataSource.componentArguments.manualLocation);
            config.setFirstActivationAsUpdate(dataSource.componentArguments.firstActivationAsUpdate);
            config.setSessionTimeout(dataSource.componentArguments.sessionTimeout);
            config.setMaxReportsCount(dataSource.componentArguments.maxReportsCount);
            config.setDispatchPeriod(dataSource.componentArguments.dispatchPeriod);
            config.setLogEnabled(dataSource.componentArguments.logEnabled);
            config.setDataSendingProperties(
                dataSource.componentArguments.dataSendingEnabled,
                dataSendingStrategy
            );
            config.setMaxReportsInDbCount(dataSource.componentArguments.maxReportsInDbCount);
            loadParametersFromStartup(config, dataSource.startupState, dataSource.componentArguments);
            return config;
        }

        @VisibleForTesting
        void loadParametersFromStartup(@NonNull ReportRequestConfig config,
                                       @NonNull StartupState startupState,
                                       @NonNull Arguments arguments) {
            loadFeatures(config, startupState);
            config.setReportHosts(startupState.getReportUrls());
            config.setClidsFromStartupResponse(startupState.getEncodedClidsFromResponse());
            config.setClidsFromClientMatchClidsFromStartupRequest(
                clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                    arguments.clidsFromClient,
                    startupState,
                    GlobalServiceLocator.getInstance().getClidsStorage()
                )
            );
        }

        void loadFeatures(ReportRequestConfig config, StartupState startupState) {
            config.setPermissionsCollectingEnabled(startupState.getCollectingFlags().permissionsCollectingEnabled);
            if (startupState.getPermissionsCollectingConfig() != null) {
                config.setPermissionsCollectingIntervalSeconds(
                    startupState.getPermissionsCollectingConfig().mCheckIntervalSeconds
                );
                config.setPermissionsForceSendIntervalSeconds(
                    startupState.getPermissionsCollectingConfig().mForceSendIntervalSeconds
                );
            }
            config.setFeaturesCollectingEnabled(startupState.getCollectingFlags().featuresCollectingEnabled);
        }

    }
}
