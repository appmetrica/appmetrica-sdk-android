package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.impl.startup.CacheControl;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.ExternalAttributionConfig;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig;
import io.appmetrica.analytics.impl.startup.StatSending;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.util.List;
import java.util.Map;

public class StartupResult {

    public enum Result {BAD, OK}

    private Result mResult;

    @NonNull
    private CollectingFlags mCollectingFlags = new CollectingFlags.CollectingFlagsBuilder().build();
    private List<String> mStartupUrls;
    private String mGetAdUrl = "";
    private List<String> mReportHostUrls;
    private String mReportAdUrl = "";
    @Nullable
    private String certificateUrl;
    private String mDeviceId;
    private String mDeviceIDHash;
    private String mEncodedClids;
    @Nullable
    private PermissionsCollectingConfig mPermissionsCollectingConfig = null;
    private Long mValidTimeDifference;
    private String mCountryInit;
    private List<String> mDiagnosticUrls;
    @Nullable
    private Map<String, List<String>> customSdkHosts;
    private StatSending mStatSending;
    @NonNull
    private RetryPolicyConfig mRetryPolicyConfig;
    @Nullable
    private BillingConfig autoInappCollectingConfig;
    private boolean mSocket;
    @NonNull
    private CacheControl mCacheControl;
    @Nullable
    private AttributionConfig attributionConfig;
    @Nullable
    private StartupUpdateConfig startupUpdateConfig;
    @NonNull
    private Map<String, Object> moduleRemoteConfigs;
    @Nullable
    private ExternalAttributionConfig externalAttributionConfig;

    @NonNull
    public CollectingFlags getCollectionFlags() {
        return mCollectingFlags;
    }

    void setCollectingFlags(@NonNull CollectingFlags collectingFlags) {
        mCollectingFlags = collectingFlags;
    }

    void setStartupUrls(List<String> startupUrls) {
        mStartupUrls = startupUrls;
    }

    public List<String> getStartupUrls() {
        return mStartupUrls;
    }

    void setCertificateUrl(@Nullable String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    @Nullable
    public String getCertificateUrl() {
        return certificateUrl;
    }

    void setGetAdUrl(String getAdUrl) {
        mGetAdUrl = getAdUrl;
    }

    public String getGetAdUrl() {
        return mGetAdUrl;
    }

    void setReportHostUrls(List<String> reportHostUrls) {
        mReportHostUrls = reportHostUrls;
    }

    public List<String> getReportHostUrls() {
        return mReportHostUrls;
    }

    void setReportAdUrl(String reportAdUrl) {
        mReportAdUrl = reportAdUrl;
    }

    public String getReportAdUrl() {
        return mReportAdUrl;
    }

    void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    void setDeviceIDHash(String deviceIDHash) {
        mDeviceIDHash = deviceIDHash;
    }

    public String getDeviceIDHash() {
        return mDeviceIDHash;
    }

    void setEncodedClids(String encodedClids) {
        mEncodedClids = encodedClids;
    }

    public String getEncodedClids() {
        return mEncodedClids;
    }

    void setResult(Result result) {
        mResult = result;
    }

    public Result getResult() {
        return mResult;
    }

    void setPermissionsCollectingConfig(@NonNull PermissionsCollectingConfig permissionsCollectingConfig) {
        mPermissionsCollectingConfig = permissionsCollectingConfig;
    }

    @Nullable
    public PermissionsCollectingConfig getPermissionsCollectingConfig() {
        return mPermissionsCollectingConfig;
    }

    void setValidTimeDifference(Long validTimeDifference) {
        mValidTimeDifference = validTimeDifference;
    }

    public Long getValidTimeDifference() {
        return mValidTimeDifference;
    }

    public String getCountryInit() {
        return mCountryInit;
    }

    void setCountryInit(String countryInit) {
        mCountryInit = countryInit;
    }

    public List<String> getDiagnosticUrls() {
        return mDiagnosticUrls;
    }

    void setDiagnosticUrls(final List<String> diagnosticUrls) {
        mDiagnosticUrls = diagnosticUrls;
    }

    void setCustomSdkHosts(@NonNull Map<String, List<String>> customSdkHosts) {
        this.customSdkHosts = customSdkHosts;
    }

    @Nullable
    public Map<String, List<String>> getCustomSdkHosts() {
        return customSdkHosts;
    }

    public StatSending getStatSending() {
        return mStatSending;
    }

    void setStatSending(StatSending statSending) {
        mStatSending = statSending;
    }

    void setRetryPolicyConfig(@NonNull RetryPolicyConfig retryPolicyConfig) {
        mRetryPolicyConfig = retryPolicyConfig;
    }

    @Nullable
    public RetryPolicyConfig getRetryPolicyConfig() {
        return mRetryPolicyConfig;
    }

    public void setAutoInappCollectingConfig(@NonNull BillingConfig autoInappCollectingConfig) {
        this.autoInappCollectingConfig = autoInappCollectingConfig;
    }

    @Nullable
    public BillingConfig getAutoInappCollectingConfig() {
        return autoInappCollectingConfig;
    }

    public void setSocketEnabled(boolean enabled) {
        mSocket = enabled;
    }

    public boolean isSocketEnabled() {
        return mSocket;
    }

    @NonNull
    public CacheControl getCacheControl() {
        return mCacheControl;
    }

    public void setCacheControl(@NonNull CacheControl cacheControl) {
        mCacheControl = cacheControl;
    }

    @Nullable
    public AttributionConfig getAttributionConfig() {
        return attributionConfig;
    }

    public void setAttributionConfig(@NonNull AttributionConfig attributionConfig) {
        this.attributionConfig = attributionConfig;
    }

    @Nullable
    public StartupUpdateConfig getStartupUpdateConfig() {
        return startupUpdateConfig;
    }

    public void setStartupUpdateConfig(@NonNull StartupUpdateConfig startupUpdateConfig){
        this.startupUpdateConfig = startupUpdateConfig;
    }

    public void setModuleRemoteConfigs(@NonNull Map<String, Object> moduleRemoteConfigs) {
        this.moduleRemoteConfigs = moduleRemoteConfigs;
    }

    public Map<String, Object> getModulesRemoteConfigs() {
        return this.moduleRemoteConfigs;
    }

    @Nullable
    public ExternalAttributionConfig getExternalAttributionConfig() {
        return externalAttributionConfig;
    }

    public void setExternalAttributionConfig(@Nullable ExternalAttributionConfig externalAttributionConfig) {
        this.externalAttributionConfig = externalAttributionConfig;
    }
}
