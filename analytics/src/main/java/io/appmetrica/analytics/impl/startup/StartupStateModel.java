package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StartupStateModel {

    //region AppMetricaDeviceIdentifiers
    @Nullable
    public final String uuid;
    //endregion

    //region Urls
    @Nullable
    public final List<String> reportUrls;
    @Nullable
    public final String getAdUrl;
    @Nullable
    public final String reportAdUrl;
    @Nullable
    public final String certificateUrl;
    @Nullable
    public final List<String> hostUrlsFromStartup;
    @Nullable
    public final List<String> hostUrlsFromClient;
    @Nullable
    public final List<String> diagnosticUrls;
    @Nullable
    public final Map<String, List<String>> customSdkHosts;
    //endregion

    @Nullable
    public final String encodedClidsFromResponse;
    @Nullable
    public final String lastClientClidsForStartupRequest;
    @Nullable
    public final String lastChosenForRequestClids;

    //region Startup collectingFlags
    @NonNull
    public final CollectingFlags collectingFlags;
    //endregion

    public final long obtainTime;
    public final boolean hadFirstStartup;
    public final boolean startupDidNotOverrideClids;

    @Nullable
    public final String countryInit;
    @Nullable
    public final StatSending statSending;

    @Nullable
    public final PermissionsCollectingConfig permissionsCollectingConfig;
    @NonNull
    public final RetryPolicyConfig retryPolicyConfig;

    public final long obtainServerTime;
    public final long firstStartupServerTime;

    public final boolean outdated;

    @Nullable
    public final BillingConfig autoInappCollectingConfig;
    @Nullable
    public final CacheControl cacheControl;
    @Nullable
    public final AttributionConfig attributionConfig;
    @NonNull
    public final StartupUpdateConfig startupUpdateConfig;
    @NonNull
    public final Map<String, Object> modulesRemoteConfigs;
    @Nullable
    public final ExternalAttributionConfig externalAttributionConfig;

    private StartupStateModel(@NonNull StartupStateBuilder builder) {
        this.uuid = builder.mUuid;
        this.reportUrls = builder.mReportUrls == null ?
            null : CollectionUtils.unmodifiableListCopy(builder.mReportUrls);
        this.getAdUrl = builder.mGetAdUrl;
        this.reportAdUrl = builder.mReportAdUrl;
        this.certificateUrl = builder.certificateUrl;
        this.hostUrlsFromStartup = builder.mHostUrlsFromStartup == null ?
            null : CollectionUtils.unmodifiableListCopy(builder.mHostUrlsFromStartup);
        this.hostUrlsFromClient = builder.mHostUrlsFromClient == null ?
            null : CollectionUtils.unmodifiableListCopy(builder.mHostUrlsFromClient);
        this.diagnosticUrls = builder.mDiagnosticUrls == null ?
            null : CollectionUtils.unmodifiableListCopy(builder.mDiagnosticUrls);
        this.customSdkHosts = builder.customSdkHosts == null ?
            null : CollectionUtils.unmodifiableMapCopy(builder.customSdkHosts);
        this.encodedClidsFromResponse = builder.mEncodedClidsFromResponse;
        this.lastClientClidsForStartupRequest = builder.mClientClidsForLastStartupRequest;
        this.collectingFlags = builder.mCollectingFlags;
        this.permissionsCollectingConfig = builder.mPermissionsCollectingConfig;
        this.obtainTime = builder.mObtainTime;
        this.hadFirstStartup = builder.mHadFirstStartup;
        this.lastChosenForRequestClids = builder.mLastChosenForRequestClids;
        this.startupDidNotOverrideClids = builder.mStartupDidNotOverrideClids;
        this.countryInit = builder.mCountryInit;
        this.statSending = builder.mStatSending;
        this.obtainServerTime = builder.mObtainServerTime;
        this.firstStartupServerTime = builder.mFirstStartupServerTime;
        this.outdated = builder.mOutdated;
        if (builder.mRetryPolicyConfig == null) {
            StartupStateProtobuf.StartupState emptyState = new StartupStateProtobuf.StartupState();
            this.retryPolicyConfig = new RetryPolicyConfig(
                emptyState.maxRetryIntervalSeconds,
                emptyState.retryExponentialMultiplier
            );
        } else {
            this.retryPolicyConfig = builder.mRetryPolicyConfig;
        }
        this.autoInappCollectingConfig = builder.autoInappCollectingConfig;
        this.cacheControl = builder.mCacheControl;
        this.attributionConfig = builder.attributionConfig;
        this.startupUpdateConfig = builder.startupUpdateConfig == null ?
            new StartupUpdateConfig(DefaultValues.STARTUP_UPDATE_CONFIG.interval) :
            builder.startupUpdateConfig;
        this.modulesRemoteConfigs = builder.modulesRemoteConfigs == null ?
            Collections.<String, Object>emptyMap() :
            builder.modulesRemoteConfigs;
        this.externalAttributionConfig = builder.externalAttributionConfig;
    }

    public StartupStateBuilder buildUpon() {
        return buildUpon(collectingFlags);
    }

    public StartupStateBuilder buildUpon(@NonNull CollectingFlags collectingFlags) {
        return new StartupStateModel.StartupStateBuilder(collectingFlags)
            .withUuid(uuid)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withHostUrlsFromClient(hostUrlsFromClient)
            .withEncodedClidsFromResponse(encodedClidsFromResponse)
            .withReportUrls(reportUrls)
            .withGetAdUrl(getAdUrl)
            .withReportAdUrl(reportAdUrl)
            .withCertificateUrl(certificateUrl)
            .withDiagnosticUrls(diagnosticUrls)
            .withCustomSdkHosts(customSdkHosts)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withLastChosenForRequestClids(lastChosenForRequestClids)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withObtainTime(obtainTime)
            .withHadFirstStartup(hadFirstStartup)
            .withCountryInit(countryInit)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withObtainServerTime(obtainServerTime)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withStatSending(statSending)
            .withOutdated(outdated)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withCacheControl(cacheControl)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
            .withAttributionConfig(attributionConfig)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesRemoteConfigs)
            .withExternalAttributionConfig(externalAttributionConfig);
    }

    @Override
    public String toString() {
        return "StartupStateModel{" +
            "uuid='" + uuid + '\'' +
            ", reportUrls=" + reportUrls +
            ", getAdUrl='" + getAdUrl + '\'' +
            ", reportAdUrl='" + reportAdUrl + '\'' +
            ", certificateUrl='" + certificateUrl + '\'' +
            ", hostUrlsFromStartup=" + hostUrlsFromStartup +
            ", hostUrlsFromClient=" + hostUrlsFromClient +
            ", diagnosticUrls=" + diagnosticUrls +
            ", customSdkHosts=" + customSdkHosts +
            ", encodedClidsFromResponse='" + encodedClidsFromResponse + '\'' +
            ", lastClientClidsForStartupRequest='" + lastClientClidsForStartupRequest + '\'' +
            ", lastChosenForRequestClids='" + lastChosenForRequestClids + '\'' +
            ", collectingFlags=" + collectingFlags +
            ", obtainTime=" + obtainTime +
            ", hadFirstStartup=" + hadFirstStartup +
            ", startupDidNotOverrideClids=" + startupDidNotOverrideClids +
            ", countryInit='" + countryInit + '\'' +
            ", statSending=" + statSending +
            ", permissionsCollectingConfig=" + permissionsCollectingConfig +
            ", retryPolicyConfig=" + retryPolicyConfig +
            ", obtainServerTime=" + obtainServerTime +
            ", firstStartupServerTime=" + firstStartupServerTime +
            ", outdated=" + outdated +
            ", autoInappCollectingConfig=" + autoInappCollectingConfig +
            ", cacheControl=" + cacheControl +
            ", attributionConfig=" + attributionConfig +
            ", startupUpdateConfig=" + startupUpdateConfig +
            ", modulesRemoteConfigs=" + modulesRemoteConfigs +
            ", externalAttributionConfig=" + externalAttributionConfig +
            '}';
    }

    public static class StartupStateBuilder {

        @Nullable
        String mUuid;
        @Nullable
        String mDeviceId;
        @Nullable
        String mDeviceIdHash;
        @Nullable
        List<String> mReportUrls;
        @Nullable
        String mGetAdUrl;
        @Nullable
        String mReportAdUrl;
        @Nullable
        String certificateUrl;
        @Nullable
        List<String> mHostUrlsFromStartup;
        @Nullable
        List<String> mHostUrlsFromClient;
        @Nullable
        List<String> mDiagnosticUrls;
        @Nullable
        Map<String, List<String>> customSdkHosts;
        @Nullable
        String mEncodedClidsFromResponse;
        @Nullable
        String mClientClidsForLastStartupRequest;
        @Nullable
        String mLastChosenForRequestClids;
        @NonNull
        final CollectingFlags mCollectingFlags;
        @Nullable
        PermissionsCollectingConfig mPermissionsCollectingConfig;
        long mObtainTime;
        boolean mHadFirstStartup;
        boolean mStartupDidNotOverrideClids;
        @Nullable
        private String mCountryInit;
        @Nullable
        StatSending mStatSending;
        private long mObtainServerTime;
        private long mFirstStartupServerTime;
        boolean mOutdated;
        @Nullable
        RetryPolicyConfig mRetryPolicyConfig;
        @Nullable
        BillingConfig autoInappCollectingConfig;
        @Nullable
        CacheControl mCacheControl;
        @Nullable
        AttributionConfig attributionConfig;
        @Nullable
        private StartupUpdateConfig startupUpdateConfig;
        @Nullable
        private Map<String, Object> modulesRemoteConfigs;
        @Nullable
        private ExternalAttributionConfig externalAttributionConfig;

        public StartupStateBuilder(@NonNull CollectingFlags collectingFlags) {
            mCollectingFlags = collectingFlags;
        }

        public StartupStateBuilder withUuid(@Nullable String uuid) {
            mUuid = uuid;
            return this;
        }

        public StartupStateBuilder withReportUrls(@Nullable List<String> reportUrls) {
            mReportUrls = reportUrls;
            return this;
        }

        public StartupStateBuilder withGetAdUrl(@Nullable String getAdUrl) {
            mGetAdUrl = getAdUrl;
            return this;
        }

        public StartupStateBuilder withReportAdUrl(@Nullable String reportAdUrl) {
            mReportAdUrl = reportAdUrl;
            return this;
        }

        public StartupStateBuilder withCertificateUrl(@Nullable String certificateUrl) {
            this.certificateUrl = certificateUrl;
            return this;
        }

        public StartupStateBuilder withHostUrlsFromStartup(@Nullable List<String> hostUrlsFromStartup) {
            mHostUrlsFromStartup = hostUrlsFromStartup;
            return this;
        }

        public StartupStateBuilder withHostUrlsFromClient(@Nullable List<String> hostUrlsFromClient) {
            mHostUrlsFromClient = hostUrlsFromClient;
            return this;
        }

        public StartupStateBuilder withDiagnosticUrls(@Nullable List<String> diagnosticUrls) {
            mDiagnosticUrls = diagnosticUrls;
            return this;
        }

        public StartupStateBuilder withCustomSdkHosts(@Nullable Map<String, List<String>> customSdkHosts) {
            this.customSdkHosts = customSdkHosts;
            return this;
        }

        public StartupStateBuilder withEncodedClidsFromResponse(@Nullable String encodedClidsFromResponse) {
            mEncodedClidsFromResponse = encodedClidsFromResponse;
            return this;
        }

        public StartupStateBuilder withLastClientClidsForStartupRequest(@Nullable String clids) {
            mClientClidsForLastStartupRequest = clids;
            return this;
        }

        public StartupStateBuilder withLastChosenForRequestClids(@Nullable String clids) {
            mLastChosenForRequestClids = clids;
            return this;
        }

        public StartupStateBuilder withPermissionsCollectingConfig(
            @Nullable PermissionsCollectingConfig permissionsCollectingConfig) {
            mPermissionsCollectingConfig = permissionsCollectingConfig;
            return this;
        }

        public StartupStateBuilder withObtainTime(long obtainTime) {
            mObtainTime = obtainTime;
            return this;
        }

        public StartupStateBuilder withHadFirstStartup(boolean hadFirstStartup) {
            mHadFirstStartup = hadFirstStartup;
            return this;
        }

        public StartupStateBuilder withStartupDidNotOverrideClids(boolean didNotOverride) {
            mStartupDidNotOverrideClids = didNotOverride;
            return this;
        }

        public StartupStateBuilder withCountryInit(@Nullable String countryInit) {
            mCountryInit = countryInit;
            return this;
        }

        public StartupStateBuilder withObtainServerTime(final long obtainServerTime) {
            mObtainServerTime = obtainServerTime;
            return this;
        }

        public StartupStateBuilder withFirstStartupServerTime(final long firstStartupServerTime) {
            mFirstStartupServerTime = firstStartupServerTime;
            return this;
        }

        public StartupStateBuilder withStatSending(StatSending statSending) {
            mStatSending = statSending;
            return this;
        }

        public StartupStateBuilder withOutdated(boolean outdated) {
            mOutdated = outdated;
            return this;
        }

        public StartupStateBuilder withRetryPolicyConfig(@Nullable RetryPolicyConfig retryPolicyConfig) {
            mRetryPolicyConfig = retryPolicyConfig;
            return this;
        }

        public StartupStateBuilder withAutoInappCollectingConfig(@Nullable BillingConfig autoInappCollectingConfig) {
            this.autoInappCollectingConfig = autoInappCollectingConfig;
            return this;
        }

        public StartupStateBuilder withCacheControl(@Nullable CacheControl cacheControl) {
            mCacheControl = cacheControl;
            return this;
        }

        public StartupStateBuilder withAttributionConfig(@Nullable AttributionConfig attributionConfig) {
            this.attributionConfig = attributionConfig;
            return this;
        }

        @NonNull
        public StartupStateBuilder withStartupUpdateConfig(@Nullable StartupUpdateConfig startupUpdateConfig) {
            this.startupUpdateConfig = startupUpdateConfig;
            return this;
        }

        @NonNull
        public StartupStateBuilder withModulesRemoteConfigs(@NonNull Map<String, Object> modulesRemoteConfigs) {
            this.modulesRemoteConfigs = modulesRemoteConfigs;
            return this;
        }

        @NonNull
        public StartupStateBuilder withExternalAttributionConfig(
            @Nullable ExternalAttributionConfig externalAttributionConfig
        ) {
            this.externalAttributionConfig = externalAttributionConfig;
            return this;
        }

        @NonNull
        public StartupStateModel build() {
            return new StartupStateModel(this);
        }
    }
}
