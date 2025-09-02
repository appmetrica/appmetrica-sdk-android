package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.startup.ClidsStateChecker;
import io.appmetrica.analytics.impl.startup.StartupState;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportRequestConfig extends CoreRequestConfig {

    @Override
    public String toString() {
        return "ReportRequestConfig{" +
            "mLocationTracking=" + locationTracking +
            ", mFirstActivationAsUpdate=" + firstActivationAsUpdate +
            ", mSessionTimeout=" + sessionTimeout +
            ", mDispatchPeriod=" + dispatchPeriod +
            ", mLogEnabled=" + logEnabled +
            ", mMaxReportsCount=" + maxReportsCount +
            ", dataSendingEnabledFromArguments=" + dataSendingEnabledFromArguments +
            ", dataSendingStrategy=" + dataSendingStrategy +
            ", mPreloadInfoSendingStrategy=" + preloadInfoSendingStrategy +
            ", mApiKey='" + apiKey + '\'' +
            ", mPermissionsCollectingEnabled=" + permissionsCollectingEnabled +
            ", mFeaturesCollectingEnabled=" + featuresCollectingEnabled +
            ", mClidsFromStartupResponse='" + clidsFromStartupResponse + '\'' +
            ", mReportHosts=" + reportHosts +
            ", mAttributionId=" + attributionId +
            ", mPermissionsCollectingIntervalSeconds=" + permissionsCollectingIntervalSeconds +
            ", mPermissionsForceSendIntervalSeconds=" + permissionsForceSendIntervalSeconds +
            ", mClidsFromClientMatchClidsFromStartupRequest=" + clidsFromClientMatchClidsFromStartupRequest +
            ", mMaxReportsInDbCount=" + maxReportsInDbCount +
            ", mCertificates=" + certificates +
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

    private boolean locationTracking;
    private boolean firstActivationAsUpdate;
    private int sessionTimeout;
    private int dispatchPeriod;
    private boolean logEnabled;
    private int maxReportsCount;
    private Boolean dataSendingEnabledFromArguments;
    private DataSendingStrategy dataSendingStrategy;
    @NonNull
    private final PreloadInfoSendingStrategy preloadInfoSendingStrategy;

    // AppMetricaDeviceIdentifiers
    private String apiKey;

    private boolean permissionsCollectingEnabled;
    private boolean featuresCollectingEnabled;

    private String clidsFromStartupResponse;

    private List<String> reportHosts;

    private int attributionId;
    private long permissionsCollectingIntervalSeconds;
    private long permissionsForceSendIntervalSeconds;
    private boolean clidsFromClientMatchClidsFromStartupRequest;
    private long maxReportsInDbCount;

    @Nullable
    private List<String> certificates;

    @NonNull
    private Set<String> autoCollectedDataSubscribers = new HashSet<>();

    @VisibleForTesting()
    ReportRequestConfig(@NonNull PreloadInfoSendingStrategy preloadInfoSendingStrategy) {
        this.preloadInfoSendingStrategy = preloadInfoSendingStrategy;
    }

    @NonNull
    public String getClidsFromStartupResponse() {
        return WrapUtils.getOrDefault(clidsFromStartupResponse, StringUtils.EMPTY);
    }

    void setClidsFromStartupResponse(final String clids) {
        clidsFromStartupResponse = clids;
    }

    public void setReportHosts(final List<String> hosts) {
        reportHosts = hosts;
    }

    public List<String> getReportHosts() {
        return reportHosts;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isPermissionsCollectingEnabled() {
        return permissionsCollectingEnabled;
    }

    public boolean isFeaturesCollectingEnabled() {
        return featuresCollectingEnabled;
    }

    public void setPermissionsCollectingIntervalSeconds(long permissionsCollectingIntervalSeconds) {
        this.permissionsCollectingIntervalSeconds = permissionsCollectingIntervalSeconds;
    }

    public long getPermissionsCollectingIntervalSeconds() {
        return permissionsCollectingIntervalSeconds;
    }

    public void setPermissionsForceSendIntervalSeconds(long permissionsForceSendIntervalSeconds) {
        this.permissionsForceSendIntervalSeconds = permissionsForceSendIntervalSeconds;
    }

    public long getPermissionsForceSendIntervalSeconds() {
        return permissionsForceSendIntervalSeconds;
    }

    public void setPermissionsCollectingEnabled(boolean value) {
        permissionsCollectingEnabled = value;
    }

    public void setFeaturesCollectingEnabled(boolean value) {
        featuresCollectingEnabled = value;
    }

    public boolean isReadyForSending() {
        return isIdentifiersValid() &&
            !Utils.isNullOrEmpty(getReportHosts()) &&
            getClidsFromClientMatchClidsFromStartupRequest();
    }

    private void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean needToSendPreloadInfo() {
        return preloadInfoSendingStrategy.shouldSend();
    }

    public boolean isLocationTracking() {
        return locationTracking;
    }

    public void setLocationTracking(boolean locationTracking) {
        this.locationTracking = locationTracking;
    }

    public boolean isFirstActivationAsUpdate() {
        return firstActivationAsUpdate;
    }

    public void setFirstActivationAsUpdate(boolean firstActivationAsUpdate) {
        this.firstActivationAsUpdate = firstActivationAsUpdate;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getDispatchPeriod() {
        return dispatchPeriod;
    }

    public void setDispatchPeriod(int dispatchPeriod) {
        this.dispatchPeriod = dispatchPeriod;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public int getMaxReportsCount() {
        return maxReportsCount;
    }

    public void setMaxReportsCount(int maxReportsCount) {
        this.maxReportsCount = maxReportsCount;
    }

    public int getAttributionId() {
        return attributionId;
    }

    public void setAttributionId(final int attributionId) {
        this.attributionId = attributionId;
    }

    public long getMaxEventsInDbCount() {
        return maxReportsInDbCount;
    }

    public void setMaxReportsInDbCount(long maxReportsInDbCount) {
        this.maxReportsInDbCount = maxReportsInDbCount;
    }

    public boolean getCurrentDataSendingState() {
        return dataSendingStrategy.shouldSend(dataSendingEnabledFromArguments);
    }

    @Nullable
    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(@NonNull List<String> certificates) {
        this.certificates = certificates;
    }

    public void setDataSendingProperties(@Nullable Boolean dataSendingFromArguments,
                                         @NonNull DataSendingStrategy dataSendingStrategy) {
        this.dataSendingEnabledFromArguments = dataSendingFromArguments;
        this.dataSendingStrategy = dataSendingStrategy;
    }

    public boolean getClidsFromClientMatchClidsFromStartupRequest() {
        return clidsFromClientMatchClidsFromStartupRequest;
    }

    public void setClidsFromClientMatchClidsFromStartupRequest(final boolean match) {
        clidsFromClientMatchClidsFromStartupRequest = match;
    }

    @NonNull
    public Set<String> getAutoCollectedDataSubscribers() {
        return autoCollectedDataSubscribers;
    }

    public void setAutoCollectedDataSubscribers(@NonNull Set<String> autoCollectedDataSubscribers) {
        this.autoCollectedDataSubscribers = autoCollectedDataSubscribers;
    }

    public static final class Arguments extends BaseRequestArguments<CommonArguments.ReporterArguments, Arguments> {

        @Nullable
        public final String apiKey;
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
                reporterArguments.apiKey,
                reporterArguments.locationTracking,
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

        Arguments(@Nullable String apiKey,
                  @Nullable Boolean locationTracking,
                  @Nullable Boolean firstActivationAsUpdate,
                  @Nullable Integer sessionTimeout,
                  @Nullable Integer maxReportsCount,
                  @Nullable Integer dispatchPeriod,
                  @Nullable Boolean logEnabled,
                  @Nullable Boolean dataSendingEnabled,
                  @Nullable Map<String, String> clidsFromClient,
                  @Nullable Integer maxReportsInDbCount) {
            super();
            this.apiKey = apiKey;
            this.locationTracking = WrapUtils.getOrDefault(locationTracking,
                DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED);
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
                null
            );
        }

        @Override
        @NonNull
        public Arguments mergeFrom(@NonNull CommonArguments.ReporterArguments other) {
            return new Arguments(
                WrapUtils.getOrDefaultNullable(other.apiKey, apiKey),
                WrapUtils.getOrDefaultNullable(
                    other.locationTracking, locationTracking),
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
                if (clidsFromClient == null || !clidsFromClient.equals(arguments.clidsFromClient)) {
                    return false;
                }
            }
            if (arguments.maxReportsInDbCount != null) {
                if (maxReportsInDbCount != arguments.maxReportsInDbCount) {
                    return false;
                }
            }
            return true;
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
            config.setAutoCollectedDataSubscribers(
                mComponentUnit.getAutoCollectedDataSubscribersHolder().getSubscribers()
            );
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
