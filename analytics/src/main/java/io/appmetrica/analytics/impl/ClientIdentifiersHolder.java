package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import java.util.Map;

public class ClientIdentifiersHolder {

    private static final String KEY_VALUE = "value";
    private static final String EXTRA_UUID = "Uuid";
    private static final String EXTRA_DEVICE_ID = "DeviceId";
    private static final String EXTRA_DEVICE_ID_HASH = "DeviceIdHash";
    private static final String EXTRA_AD_URL_GET = "AdUrlGet";
    private static final String EXTRA_AD_URL_REPORT = "AdUrlReport";
    private static final String EXTRA_RESPONSE_CLIDS = "Clids";
    private static final String EXTRA_REQUEST_CLIDS = "RequestClids";
    private static final String EXTRA_SERVER_TIME_OFFSET = "ServerTimeOffset";
    private static final String EXTRA_GAID = "GAID";
    private static final String EXTRA_HOAID = "HOAID";
    private static final String EXTRA_YANDEX_ADV_ID = "YANDEX_ADV_ID";
    private static final String EXTRA_CUSTOM_SDK_HOSTS = "CUSTOM_SDK_HOSTS";
    private static final String EXTRA_NEXT_STARTUP_TIME = "NextStartupTime";
    private static final String EXTRA_FEATURES = "features";
    private static final String NO_IDENTIFIER_IN_STARTUP_STATE = "no identifier in startup state";
    private static final String BUNDLE_SREIALIZATION_ERROR = "bundle serialization error";

    @NonNull
    private final IdentifiersResult mUuidData;
    @NonNull
    private final IdentifiersResult mDeviceIdData;
    @NonNull
    private final IdentifiersResult mDeviceIdHashData;
    @NonNull
    private final IdentifiersResult mReportAdUrlData;
    @NonNull
    private final IdentifiersResult mGetAdUrlData;
    @NonNull
    private final IdentifiersResult mResponseClidsData;
    @NonNull
    private final IdentifiersResult mClientClidsForRequestData;
    @NonNull
    private final IdentifiersResult mGaidData;
    @NonNull
    private final IdentifiersResult mHoaidData;
    @NonNull
    private final IdentifiersResult yandexAdvIdData;
    @NonNull
    private final IdentifiersResult customSdkHostsData;
    private final long mServerTimeOffset;
    private final long nextStartupTime;
    @NonNull
    private final FeaturesInternal features;

    ClientIdentifiersHolder(@NonNull StartupState startupState,
                            @NonNull AdvertisingIdsHolder advertisingIdsHolder,
                            @Nullable Map<String, String> clientClids) {
        this(
                createIdentifierData(startupState.getUuid()),
                createIdentifierData(startupState.getDeviceId()),
                createIdentifierData(startupState.getDeviceIdHash()),
                createIdentifierData(startupState.getReportAdUrl()),
                createIdentifierData(startupState.getGetAdUrl()),
                createIdentifierData(JsonHelper.clidsToString(
                        StartupUtils.decodeClids(startupState.getEncodedClidsFromResponse())
                )),
                createIdentifierData(JsonHelper.clidsToString(clientClids)),
                new IdentifiersResult(
                        advertisingIdsHolder.getGoogle().mAdTrackingInfo == null ?
                                null :
                                advertisingIdsHolder.getGoogle().mAdTrackingInfo.advId,
                        advertisingIdsHolder.getGoogle().mStatus,
                        advertisingIdsHolder.getGoogle().mErrorExplanation
                ),
                new IdentifiersResult(
                        advertisingIdsHolder.getHuawei().mAdTrackingInfo == null ?
                                null :
                                advertisingIdsHolder.getHuawei().mAdTrackingInfo.advId,
                        advertisingIdsHolder.getHuawei().mStatus,
                        advertisingIdsHolder.getHuawei().mErrorExplanation
                ),
                new IdentifiersResult(
                        advertisingIdsHolder.getYandex().mAdTrackingInfo == null ?
                                null :
                                advertisingIdsHolder.getYandex().mAdTrackingInfo.advId,
                        advertisingIdsHolder.getYandex().mStatus,
                        advertisingIdsHolder.getYandex().mErrorExplanation
                ),
                createIdentifierData(JsonHelper.customSdkHostsToString(startupState.getCustomSdkHosts())),
                TimeUtils.getServerTimeOffset(),
                startupState.getObtainTime() + startupState.getStartupUpdateConfig().getIntervalSeconds(),
                createFeaturesInternal(startupState.getCollectingFlags().sslPinning)
        );
    }

    public ClientIdentifiersHolder(@NonNull Bundle bundle) {
        this(
                parseIdentifiersData(bundle, EXTRA_UUID),
                parseIdentifiersData(bundle, EXTRA_DEVICE_ID),
                parseIdentifiersData(bundle, EXTRA_DEVICE_ID_HASH),
                parseIdentifiersData(bundle, EXTRA_AD_URL_REPORT),
                parseIdentifiersData(bundle, EXTRA_AD_URL_GET),
                parseIdentifiersData(bundle, EXTRA_RESPONSE_CLIDS),
                parseIdentifiersData(bundle, EXTRA_REQUEST_CLIDS),
                parseIdentifiersData(bundle, EXTRA_GAID),
                parseIdentifiersData(bundle, EXTRA_HOAID),
                parseIdentifiersData(bundle, EXTRA_YANDEX_ADV_ID),
                parseIdentifiersData(bundle, EXTRA_CUSTOM_SDK_HOSTS),
                bundle.getLong(EXTRA_SERVER_TIME_OFFSET),
                bundle.getLong(EXTRA_NEXT_STARTUP_TIME),
                parseFeaturesInternal(bundle, EXTRA_FEATURES)
        );
    }

    public ClientIdentifiersHolder(@NonNull IdentifiersResult uuidData,
                                   @NonNull IdentifiersResult deviceIdData,
                                   @NonNull IdentifiersResult deviceIdHashData,
                                   @NonNull IdentifiersResult reportAdUrlData,
                                   @NonNull IdentifiersResult getAdUrlData,
                                   @NonNull IdentifiersResult responseClidsData,
                                   @NonNull IdentifiersResult clientClidsForRequestData,
                                   @NonNull IdentifiersResult gaidData,
                                   @NonNull IdentifiersResult hoaidData,
                                   @NonNull IdentifiersResult yandexAdvIdData,
                                   @NonNull IdentifiersResult customSdkHostsData,
                                   final long serverTimeOffset,
                                   final long nextStartupTime,
                                   @NonNull FeaturesInternal features) {
        mUuidData = uuidData;
        mDeviceIdData = deviceIdData;
        mDeviceIdHashData = deviceIdHashData;
        mReportAdUrlData = reportAdUrlData;
        mGetAdUrlData = getAdUrlData;
        mResponseClidsData = responseClidsData;
        mClientClidsForRequestData = clientClidsForRequestData;
        mGaidData = gaidData;
        mHoaidData = hoaidData;
        this.yandexAdvIdData = yandexAdvIdData;
        this.customSdkHostsData = customSdkHostsData;
        mServerTimeOffset = serverTimeOffset;
        this.nextStartupTime = nextStartupTime;
        this.features = features;
    }

    @NonNull
    public IdentifiersResult getUuid() {
        return mUuidData;
    }

    @NonNull
    public IdentifiersResult getDeviceId() {
        return mDeviceIdData;
    }

    @NonNull
    public IdentifiersResult getDeviceIdHash() {
        return mDeviceIdHashData;
    }

    @NonNull
    public IdentifiersResult getReportAdUrl() {
        return mReportAdUrlData;
    }

    @NonNull
    public IdentifiersResult getGetAdUrl() {
        return mGetAdUrlData;
    }

    @NonNull
    public IdentifiersResult getResponseClids() {
        return mResponseClidsData;
    }

    @NonNull
    public IdentifiersResult getClientClidsForRequest() {
        return mClientClidsForRequestData;
    }

    @NonNull
    public IdentifiersResult getGaid() {
        return mGaidData;
    }

    @NonNull
    public IdentifiersResult getHoaid() {
        return mHoaidData;
    }

    @NonNull
    public IdentifiersResult getYandexAdvId() {
        return yandexAdvIdData;
    }

    @NonNull
    public IdentifiersResult getCustomSdkHosts() {
        return customSdkHostsData;
    }

    @NonNull
    public FeaturesInternal getFeatures() {
        return features;
    }

    public long getServerTimeOffset() {
        return mServerTimeOffset;
    }

    public long getNextStartupTime() {
        return nextStartupTime;
    }

    public void toBundle(@NonNull Bundle bundle) {
        bundle.putBundle(EXTRA_UUID, getBundleWithParcelable(mUuidData));
        bundle.putBundle(EXTRA_DEVICE_ID, getBundleWithParcelable(mDeviceIdData));
        bundle.putBundle(EXTRA_DEVICE_ID_HASH, getBundleWithParcelable(mDeviceIdHashData));
        bundle.putBundle(EXTRA_AD_URL_REPORT, getBundleWithParcelable(mReportAdUrlData));
        bundle.putBundle(EXTRA_AD_URL_GET, getBundleWithParcelable(mGetAdUrlData));
        bundle.putBundle(EXTRA_RESPONSE_CLIDS, getBundleWithParcelable(mResponseClidsData));
        bundle.putBundle(EXTRA_REQUEST_CLIDS, getBundleWithParcelable(mClientClidsForRequestData));
        bundle.putBundle(EXTRA_GAID, getBundleWithParcelable(mGaidData));
        bundle.putBundle(EXTRA_HOAID, getBundleWithParcelable(mHoaidData));
        bundle.putBundle(EXTRA_YANDEX_ADV_ID, getBundleWithParcelable(yandexAdvIdData));
        bundle.putBundle(EXTRA_CUSTOM_SDK_HOSTS, getBundleWithParcelable(customSdkHostsData));
        bundle.putLong(EXTRA_SERVER_TIME_OFFSET, mServerTimeOffset);
        bundle.putLong(EXTRA_NEXT_STARTUP_TIME, nextStartupTime);
        bundle.putBundle(EXTRA_FEATURES, getBundleWithParcelable(features));
    }

    @NonNull
    private static Bundle getBundleWithParcelable(@NonNull Parcelable parcelable) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VALUE, parcelable);
        return bundle;
    }

    @Nullable
    private static Parcelable getParcelableFromBundle(@Nullable Bundle bundle, @Nullable ClassLoader classLoader) {
        if (bundle == null) {
            return null;
        }
        bundle.setClassLoader(classLoader);
        return bundle.getParcelable(KEY_VALUE);
    }

    @NonNull
    private static IdentifiersResult parseIdentifiersData(@NonNull Bundle bundle, @NonNull String key) {
        IdentifiersResult result = (IdentifiersResult) getParcelableFromBundle(
                bundle.getBundle(key),
                IdentifiersResult.class.getClassLoader()
        );
        return result == null ? new IdentifiersResult(
                null,
                IdentifierStatus.UNKNOWN,
                BUNDLE_SREIALIZATION_ERROR
        ) : result;
    }

    @NonNull
    private static FeaturesInternal parseFeaturesInternal(@NonNull Bundle bundle, @NonNull String key) {
        FeaturesInternal result = (FeaturesInternal) getParcelableFromBundle(
                bundle.getBundle(key),
                FeaturesInternal.class.getClassLoader()
        );
        return result == null ? new FeaturesInternal(
                null,
                IdentifierStatus.UNKNOWN,
                BUNDLE_SREIALIZATION_ERROR
        ) : result;
    }

    @NonNull
    private static IdentifiersResult createIdentifierData(@Nullable String data) {
        boolean isDataEmpty = TextUtils.isEmpty(data);
        return new IdentifiersResult(
                data,
                isDataEmpty ? IdentifierStatus.UNKNOWN : IdentifierStatus.OK,
                isDataEmpty ? NO_IDENTIFIER_IN_STARTUP_STATE : null
        );
    }

    @NonNull
    private static FeaturesInternal createFeaturesInternal(@Nullable Boolean sslPinning) {
        boolean hasData = sslPinning != null;
        return new FeaturesInternal(
                sslPinning,
                hasData ? IdentifierStatus.OK : IdentifierStatus.UNKNOWN,
                hasData ? null : NO_IDENTIFIER_IN_STARTUP_STATE
        );
    }

    @Override
    public String toString() {
        return "ClientIdentifiersHolder{" +
                "mUuidData=" + mUuidData +
                ", mDeviceIdData=" + mDeviceIdData +
                ", mDeviceIdHashData=" + mDeviceIdHashData +
                ", mReportAdUrlData=" + mReportAdUrlData +
                ", mGetAdUrlData=" + mGetAdUrlData +
                ", mResponseClidsData=" + mResponseClidsData +
                ", mClientClidsForRequestData=" + mClientClidsForRequestData +
                ", mGaidData=" + mGaidData +
                ", mHoaidData=" + mHoaidData +
                ", yandexAdvIdData=" + yandexAdvIdData +
                ", customSdkHostsData=" + customSdkHostsData +
                ", customSdkHosts=" + customSdkHostsData +
                ", mServerTimeOffset=" + mServerTimeOffset +
                ", nextStartupTime=" + nextStartupTime +
                ", features=" + features +
                '}';
    }
}
