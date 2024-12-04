package io.appmetrica.analytics.networktasks.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.coreapi.internal.model.SdkEnvironment;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
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
            ", sdkEnvironment=" + sdkEnvironment +
            ", mProtocolVersion='" + mProtocolVersion + '\'' +
            ", sdkIdentifiers=" + sdkIdentifiers +
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

    @Nullable
    private SdkEnvironment sdkEnvironment;

    private final String mProtocolVersion = "2";

    @Nullable
    private SdkIdentifiers sdkIdentifiers;

    @Nullable
    private AppSetId appSetId;

    private RetryPolicyConfig retryPolicyConfig;

    protected BaseRequestConfig() {
    }

    public RetryPolicyConfig getRetryPolicyConfig() {
        return retryPolicyConfig;
    }

    public synchronized boolean isIdentifiersValid() {
        return sdkIdentifiers != null && ConfigUtils.areMainIdentifiersValid(sdkIdentifiers);
    }

    protected void setRetryPolicyConfig(RetryPolicyConfig retryPolicyConfig) {
        this.retryPolicyConfig = retryPolicyConfig;
    }

    protected void setSdkIdentifiers(@Nullable SdkIdentifiers sdkIdentifiers) {
        this.sdkIdentifiers = sdkIdentifiers;
    }

    @NonNull
    public String getManufacturer() {
        return WrapUtils.getOrDefault(ConstantDeviceInfo.MANUFACTURER, StringUtils.EMPTY);
    }

    public String getProtocolVersion() {
        return mProtocolVersion;
    }

    public String getAnalyticsSdkVersionName() {
        return sdkEnvironment == null ? StringUtils.EMPTY : sdkEnvironment.getSdkInfo().getSdkVersionName();
    }

    public String getAnalyticsSdkBuildNumber() {
        return sdkEnvironment == null ? StringUtils.EMPTY  : sdkEnvironment.getSdkInfo().getSdkBuildNumber();
    }

    @NonNull
    public String getAnalyticsSdkBuildType() {
        return sdkEnvironment == null ? StringUtils.EMPTY  : sdkEnvironment.getSdkInfo().getSdkBuildType();
    }

    public String getAppPlatform() {
        return ConstantDeviceInfo.APP_PLATFORM;
    }

    @NonNull
    public String getModel() {
        return ConstantDeviceInfo.MODEL;
    }

    @NonNull
    public String getOsVersion() {
        return ConstantDeviceInfo.OS_VERSION;
    }

    public int getOsApiLevel() {
        return ConstantDeviceInfo.OS_API_LEVEL;
    }
    
    public String getAppBuildNumber() {
        return sdkEnvironment == null ? StringUtils.EMPTY : sdkEnvironment.getAppVersionInfo().getAppBuildNumber();
    }

    public String getAppVersion() {
        return sdkEnvironment == null ? StringUtils.EMPTY : sdkEnvironment.getAppVersionInfo().getAppVersionName();
    }

    @NonNull
    public synchronized String getDeviceId() {
        String deviceId = StringUtils.EMPTY;
        if (sdkIdentifiers != null && sdkIdentifiers.getDeviceId() != null) {
            deviceId = sdkIdentifiers.getDeviceId();
        }
        return deviceId;
    }

    protected void setAppSetId(@NonNull AppSetId appSetId) {
        this.appSetId = appSetId;
    }

    @NonNull
    public synchronized String getAppSetId() {
        return appSetId == null || appSetId.getId() == null ? StringUtils.EMPTY : appSetId.getId();
    }

    @NonNull
    public synchronized String getAppSetIdScope() {
        return appSetId == null ? StringUtils.EMPTY : appSetId.getScope().getValue();
    }

    @NonNull
    public synchronized String getUuid() {
        String uuid = StringUtils.EMPTY;
        if (sdkIdentifiers != null && sdkIdentifiers.getUuid() != null) {
            uuid = sdkIdentifiers.getUuid();
        }
        return uuid;
    }

    @NonNull
    public synchronized String getDeviceIDHash() {
        String deviceIdHash = StringUtils.EMPTY;
        if (sdkIdentifiers != null && sdkIdentifiers.getDeviceIdHash() != null) {
            deviceIdHash = sdkIdentifiers.getDeviceIdHash();
        }
        return deviceIdHash;
    }

    @NonNull
    public String getDeviceRootStatus() {
        return ConstantDeviceInfo.DEVICE_ROOT_STATUS;
    }

    @NonNull
    public String getAppFramework() {
        return sdkEnvironment == null ? StringUtils.EMPTY : sdkEnvironment.getAppFramework();
    }

    public int getScreenWidth() {
        return sdkEnvironment == null ? 0 : sdkEnvironment.getScreenInfo().getWidth();
    }

    public int getScreenHeight() {
        return sdkEnvironment == null ? 0 : sdkEnvironment.getScreenInfo().getHeight();
    }

    public int getScreenDpi() {
        return sdkEnvironment == null ? 0 : sdkEnvironment.getScreenInfo().getDpi();
    }

    public float getScaleFactor() {
        return sdkEnvironment == null ? 0 : sdkEnvironment.getScreenInfo().getScaleFactor();
    }

    @NonNull
    public String getLocale() {
        String locale = StringUtils.EMPTY;
        if (sdkEnvironment != null) {
            String candidate = CollectionUtils.getFirstOrNull(sdkEnvironment.getLocales());
            if (candidate != null) {
                locale = candidate;
            }
        }
        return locale;
    }

    @NonNull
    public String getDeviceType() {
        return sdkEnvironment != null ? sdkEnvironment.getDeviceType() : DeviceTypeValues.PHONE;
    }

    protected void setSdkEnvironment(@NonNull SdkEnvironment sdkEnvironment) {
        this.sdkEnvironment = sdkEnvironment;
    }

    public static class DataSource<A> {

        @NonNull
        public final SdkIdentifiers sdkIdentifiers;
        @NonNull
        public final SdkEnvironmentProvider sdkEnvironmentProvider;
        @NonNull
        public final PlatformIdentifiers platformIdentifiers;
        @NonNull
        public final A componentArguments;

        public DataSource(@NonNull SdkIdentifiers sdkIdentifiers,
                          @NonNull SdkEnvironmentProvider sdkEnvironmentProvider,
                          @NonNull PlatformIdentifiers platformIdentifiers,
                          @NonNull A arguments) {
            this.sdkIdentifiers = sdkIdentifiers;
            this.componentArguments = arguments;
            this.sdkEnvironmentProvider = sdkEnvironmentProvider;
            this.platformIdentifiers = platformIdentifiers;
        }
    }

    public abstract static class BaseRequestArguments<I, O> implements ArgumentsMerger<I, O> {}

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

            config.setSdkIdentifiers(dataSource.sdkIdentifiers);

            SdkEnvironmentProvider sdkEnvironmentProvider = dataSource.sdkEnvironmentProvider;
            config.setSdkEnvironment(sdkEnvironmentProvider.getSdkEnvironment());
            PlatformIdentifiers platformIdentifiers = dataSource.platformIdentifiers;
            config.setAppSetId(platformIdentifiers.getAppSetIdProvider().getAppSetId());

            config.setPackageName(mPackageName);

            return config;
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
