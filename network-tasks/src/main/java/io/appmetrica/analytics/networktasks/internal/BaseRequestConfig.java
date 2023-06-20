package io.appmetrica.analytics.networktasks.internal;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils;
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.system.ConstantDeviceInfo;
import io.appmetrica.analytics.networktasks.impl.utils.ConfigUtils;

/*
    This class and all subclasses merges config from startup, client and device.
    Mostly, all fields took from immutable state objects (StartupState, ConstantDeviceInfo).
    But fields, that could be set from different sources, stored as separate fields.
 */
public class BaseRequestConfig {

    @Override
    public String toString() {
        return "BaseRequestConfig{" +
            "mPackageName='" + mPackageName + '\'' +
            ", mConstantDeviceInfo=" + mConstantDeviceInfo +
            ", screenInfo=" + screenInfo +
            ", mSdkVersionName='" + mSdkVersionName + '\'' +
            ", mSdkBuildNumber='" + mSdkBuildNumber + '\'' +
            ", mSdkBuildType='" + mSdkBuildType + '\'' +
            ", mAppPlatform='" + mAppPlatform + '\'' +
            ", mProtocolVersion='" + mProtocolVersion + '\'' +
            ", mAppFramework='" + mAppFramework + '\'' +
            ", mAppVersion='" + mAppVersion + '\'' +
            ", mAppBuildNumber='" + mAppBuildNumber + '\'' +
            ", appSetId=" + appSetId +
            ", mAdvertisingIdsHolder=" + mAdvertisingIdsHolder +
            ", mDeviceType='" + mDeviceType + '\'' +
            ", mLocale='" + mLocale + '\'' +
            ", identifiers=" + identifiers +
            ", retryPolicyConfig=" + retryPolicyConfig +
            '}';
    }

    public interface RequestConfigLoader<T extends BaseRequestConfig, D> {
        @NonNull
        T load(D dataSource);
    }

    private String mPackageName;

    public String getPackageName() {
        return mPackageName;
    }

    protected void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    private ConstantDeviceInfo mConstantDeviceInfo;
    private ScreenInfo screenInfo;

    private final String mSdkVersionName;
    private final String mSdkBuildNumber;
    @NonNull
    private final String mSdkBuildType;
    private final String mAppPlatform = ConstantDeviceInfo.APP_PLATFORM;
    private final String mProtocolVersion = "2";
    @NonNull private String mAppFramework;
    // Application's components versions
    private String mAppVersion;
    private String mAppBuildNumber;

    private AppSetId appSetId;

    @NonNull
    private AdvertisingIdsHolder mAdvertisingIdsHolder;

    // Device configuration
    @Nullable
    private String mDeviceType;

    private String mLocale;

    @Nullable
    private Identifiers identifiers;

    private RetryPolicyConfig retryPolicyConfig;

    protected BaseRequestConfig() {
        NetworkAppContext networkAppContext = NetworkServiceLocator.getInstance().getNetworkAppContext();
        SdkInfo sdkInfo = networkAppContext.getSdkInfo();
        mSdkVersionName = sdkInfo.getSdkVersionName();
        mSdkBuildNumber = sdkInfo.getSdkBuildNumber();
        mSdkBuildType = sdkInfo.getSdkBuildType();
        mAppFramework = FrameworkDetector.framework();
    }

    public RetryPolicyConfig getRetryPolicyConfig() {
        return retryPolicyConfig;
    }

    public synchronized boolean isIdentifiersValid() {
        return identifiers != null && ConfigUtils.areMainIdentifiersValid(identifiers);
    }

    protected void setConstantDeviceInfo(ConstantDeviceInfo constantDeviceInfo) {
        mConstantDeviceInfo = constantDeviceInfo;
    }

    protected void setScreenInfo(@NonNull ScreenInfo screenInfo) {
        this.screenInfo = screenInfo;
    }

    protected void setRetryPolicyConfig(RetryPolicyConfig retryPolicyConfig) {
        this.retryPolicyConfig = retryPolicyConfig;
    }

    protected void setIdentifiers(@Nullable Identifiers identifiers) {
        this.identifiers = identifiers;
    }

    @NonNull
    public String getManufacturer() {
        return WrapUtils.getOrDefault(mConstantDeviceInfo.manufacturer, StringUtils.EMPTY);
    }

    public String getProtocolVersion() {
        return mProtocolVersion;
    }

    public String getAnalyticsSdkVersionName() {
        return mSdkVersionName;
    }

    public String getKitBuildNumber() {
        return mSdkBuildNumber;
    }

    @NonNull
    public String getKitBuildType() {
        return mSdkBuildType;
    }

    public String getAppPlatform() {
        return mAppPlatform;
    }

    @NonNull
    public String getModel() {
        return mConstantDeviceInfo.model;
    }

    @NonNull
    public String getOsVersion() {
        return mConstantDeviceInfo.osVersion;
    }

    public int getOsApiLevel() {
        return mConstantDeviceInfo.osApiLevel;
    }

    protected void setAppBuildNumber(@Nullable final String appBuildNumber) {
        if (TextUtils.isEmpty(appBuildNumber) == false) {
            mAppBuildNumber = appBuildNumber;
        }
    }

    public String getAppBuildNumber() {
        return WrapUtils.getOrDefault(mAppBuildNumber, StringUtils.EMPTY);
    }

    public int getAppBuildNumberInt() {
        return ParseUtils.parseIntOrZero(mAppBuildNumber);
    }

    public String getAppVersion() {
        return WrapUtils.getOrDefault(mAppVersion, StringUtils.EMPTY);
    }

    protected void setAppVersion(@Nullable final String appVersion) {
        if (TextUtils.isEmpty(appVersion) == false) {
            mAppVersion = appVersion;
        }
    }

    @NonNull
    public synchronized String getDeviceId() {
        return WrapUtils.getOrDefault(identifiers == null ? null : identifiers.getDeviceId(), StringUtils.EMPTY);
    }

    @NonNull
    public synchronized String getAppSetId() {
        return WrapUtils.getOrDefault(appSetId == null ? null : appSetId.getId(), StringUtils.EMPTY);
    }

    @NonNull
    public synchronized String getAppSetIdScope() {
        return WrapUtils.getOrDefault(appSetId == null ? null : appSetId.getScope().getValue(), StringUtils.EMPTY);
    }

    public synchronized void setAppSetId(@NonNull AppSetId appSetId) {
        this.appSetId = appSetId;
    }

    @NonNull
    public synchronized String getUuid() {
        return WrapUtils.getOrDefault(identifiers == null ? null : identifiers.getUuid(), StringUtils.EMPTY);
    }

    @NonNull
    public synchronized String getDeviceIDHash() {
        return WrapUtils.getOrDefault(identifiers == null ? null : identifiers.getDeviceIdHash(), StringUtils.EMPTY);
    }

    @NonNull
    public String getDeviceRootStatus() {
        return mConstantDeviceInfo.deviceRootStatus;
    }

    @NonNull
    public String getAppFramework() {
        return mAppFramework;
    }

    public int getScreenWidth() {
        return screenInfo.getWidth();
    }

    public int getScreenHeight() {
        return screenInfo.getHeight();
    }

    public int getScreenDpi() {
        return screenInfo.getDpi();
    }

    public float getScaleFactor() {
        return screenInfo.getScaleFactor();
    }

    @NonNull
    public String getLocale() {
        return WrapUtils.getOrDefault(mLocale, StringUtils.EMPTY);
    }

    protected final void setLocale(String locale) {
        mLocale = locale;
    }

    @NonNull
    public String getDeviceType() {
        return WrapUtils.getOrDefault(mDeviceType, DeviceTypeValues.PHONE);
    }

    protected void setDeviceType(@Nullable String deviceType) {
        mDeviceType = deviceType;
    }

    @NonNull
    public AdvertisingIdsHolder getAdvertisingIdsHolder() {
        return mAdvertisingIdsHolder;
    }

    protected void setAdvertisingIdsHolder(@NonNull AdvertisingIdsHolder advertisingIdsHolder) {
        mAdvertisingIdsHolder = advertisingIdsHolder;
    }

    public static class DataSource<A> {

        @NonNull
        public final Identifiers identifiers;
        @NonNull
        public final A componentArguments;

        public DataSource(@NonNull Identifiers identifiers, A arguments) {
            this.identifiers = identifiers;
            this.componentArguments = arguments;
        }
    }

    public abstract static class BaseRequestArguments<I, O> implements
        ArgumentsMerger<I, O> {

        @Nullable
        public final String deviceType;
        @Nullable
        public final String appVersion;
        @Nullable
        public final String appBuildNumber;

        public BaseRequestArguments(@Nullable String deviceType,
                                    @Nullable String appVersion,
                                    @Nullable String appBuildNumber) {
            this.deviceType = deviceType;
            this.appVersion = appVersion;
            this.appBuildNumber = appBuildNumber;
        }

    }

    public abstract static class ComponentLoader
        <T extends BaseRequestConfig, A extends BaseRequestArguments, D extends DataSource<A>>
            implements RequestConfigLoader<T, D> {

        @NonNull
        final Context mContext;
        @NonNull
        final String mPackageName;

        protected ComponentLoader(@NonNull Context context, @NonNull String packageName) {
            mContext = context;
            mPackageName = packageName;
        }

        @NonNull
        protected abstract T createBlankConfig();

        @NonNull
        public T load(@NonNull D dataSource) {
            final T config = createBlankConfig();

            ConstantDeviceInfo constantDeviceInfo = ConstantDeviceInfo.getInstance();
            NetworkAppContext networkAppContext = NetworkServiceLocator.getInstance().getNetworkAppContext();
            ScreenInfo screenInfo = networkAppContext.getScreenInfoProvider().getScreenInfo();

            config.setConstantDeviceInfo(constantDeviceInfo);
            config.setScreenInfo(screenInfo);
            config.setIdentifiers(dataSource.identifiers);

            config.setDeviceType(loadDeviceType(dataSource.componentArguments.deviceType, screenInfo));

            loadAppVersion(config, dataSource.componentArguments.appVersion, mContext);
            loadAppBuildNumber(config, dataSource.componentArguments.appBuildNumber, mContext);

            config.setPackageName(mPackageName);
            config.setAdvertisingIdsHolder(networkAppContext.getAdvertisingIdGetter().getIdentifiers(mContext));
            config.setAppSetId(networkAppContext.getAppSetIdProvider().getAppSetId());
            config.setLocale(CollectionUtils.getFirstOrNull(networkAppContext.getLocaleProvider().getLocales()));
            return config;
        }

        private void loadAppVersion(@NonNull T config,
                                    @Nullable String passedAppVersion,
                                    @NonNull final Context context) {
            String appVersion = passedAppVersion;
            if (TextUtils.isEmpty(appVersion)) {
                appVersion = PackageManagerUtils.getAppVersionName(context);
            }

            config.setAppVersion(appVersion);
        }

        private void loadAppBuildNumber(@NonNull T config,
                                        @Nullable String passedAppBuildNumber,
                                        @NonNull Context context) {
            String appBuildNumber = passedAppBuildNumber;
            if (TextUtils.isEmpty(appBuildNumber)) {
                appBuildNumber = PackageManagerUtils.getAppVersionCodeString(context);
            }

            config.setAppBuildNumber(appBuildNumber);
        }

        @VisibleForTesting
        @Nullable
        String loadDeviceType(@Nullable String appDeviceType, @NonNull ScreenInfo screenInfo) {
            String deviceType = null;
            if (appDeviceType != null) {
                deviceType = appDeviceType;
            } else {
                deviceType = screenInfo.getDeviceType();
            }
            return deviceType;
        }

        @NonNull
        public Context getContext() {
            return mContext;
        }

        @NonNull
        public String getPackageName() {
            return mPackageName;
        }
    }
}
