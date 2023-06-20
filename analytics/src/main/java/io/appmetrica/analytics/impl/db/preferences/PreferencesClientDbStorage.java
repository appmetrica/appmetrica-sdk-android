package io.appmetrica.analytics.impl.db.preferences;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import java.util.List;
import org.json.JSONObject;

public class PreferencesClientDbStorage extends PreferencesDbStorage {
    private static final String TAG = "[PreferencesClientDbStorage]";

    static final PreferencesItem UUID_RESULT = new PreferencesItem("UUID_RESULT");
    static final PreferencesItem DEVICE_ID_RESULT = new PreferencesItem("DEVICE_ID_RESULT");
    static final PreferencesItem DEVICE_ID_HASH_RESULT = new PreferencesItem("DEVICE_ID_HASH_RESULT");
    static final PreferencesItem AD_URL_GET_RESULT = new PreferencesItem("AD_URL_GET_RESULT");
    static final PreferencesItem AD_URL_REPORT_RESULT = new PreferencesItem("AD_URL_REPORT_RESULT");
    static final PreferencesItem CUSTOM_HOSTS = new PreferencesItem("CUSTOM_HOSTS");
    static final PreferencesItem SERVER_TIME_OFFSET = new PreferencesItem("SERVER_TIME_OFFSET");
    static final PreferencesItem RESPONSE_CLIDS_RESULT = new PreferencesItem("RESPONSE_CLIDS_RESULT");
    static final PreferencesItem CUSTOM_SDK_HOSTS = new PreferencesItem("CUSTOM_SDK_HOSTS");
    static final PreferencesItem CLIENT_CLIDS = new PreferencesItem("CLIENT_CLIDS");
    static final PreferencesItem DEFERRED_DEEP_LINK_WAS_CHECKED = new PreferencesItem("DEFERRED_DEEP_LINK_WAS_CHECKED");

    static final PreferencesItem API_LEVEL = new PreferencesItem("API_LEVEL");
    static final PreferencesItem NEXT_STARTUP_TIME = new PreferencesItem("NEXT_STARTUP_TIME");
    static final PreferencesItem GAID = new PreferencesItem("GAID");
    static final PreferencesItem HOAID = new PreferencesItem("HOAID");
    static final PreferencesItem YANDEX_ADV_ID = new PreferencesItem("YANDEX_ADV_ID");
    static final PreferencesItem CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE =
            new PreferencesItem("CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE");
    static final PreferencesItem SCREEN_INFO = new PreferencesItem("SCREEN_INFO");
    static final PreferencesItem SCREEN_SIZE_CHECKED_BY_DEPRECATED =
            new PreferencesItem("SCREEN_SIZE_CHECKED_BY_DEPRECATED");
    static final PreferencesItem FEATURES = new PreferencesItem("FEATURES");

    public PreferencesClientDbStorage(final IKeyValueTableDbHelper dbHelper) {
        super(dbHelper);
    }

    @NonNull
    public IdentifiersResult getUuidResult() {
        return getAdsIdentifiersResult(UUID_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getDeviceIdResult() {
        return getAdsIdentifiersResult(DEVICE_ID_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getDeviceIdHashResult() {
        return getAdsIdentifiersResult(DEVICE_ID_HASH_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getAdUrlGetResult() {
        return getAdsIdentifiersResult(AD_URL_GET_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getAdUrlReportResult() {
        return getAdsIdentifiersResult(AD_URL_REPORT_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getCustomSdkHosts() {
        return getAdsIdentifiersResult(CUSTOM_SDK_HOSTS.fullKey());
    }

    @NonNull
    public FeaturesInternal getFeatures() {
        String features = readString(FEATURES.fullKey(), null);
        return JsonHelper.featuresFromJson(features);
    }

    @NonNull
    public IdentifiersResult getResponseClidsResult() {
        return getAdsIdentifiersResult(RESPONSE_CLIDS_RESULT.fullKey());
    }

    @NonNull
    public IdentifiersResult getGaid() {
        return getAdsIdentifiersResult(GAID.fullKey());
    }

    @NonNull
    public IdentifiersResult getHoaid() {
        return getAdsIdentifiersResult(HOAID.fullKey());
    }

    @NonNull
    public IdentifiersResult getYandexAdvId() {
        return getAdsIdentifiersResult(YANDEX_ADV_ID.fullKey());
    }

    public boolean getClientClidsChangedAfterLastIdentifiersUpdate(boolean defaultValue) {
        return readBoolean(CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE.fullKey(), defaultValue);
    }

    public List<String> getCustomHosts() {
        String hosts = readString(CUSTOM_HOSTS.fullKey(), null);
        List<String> result = TextUtils.isEmpty(hosts) ? null : JsonHelper.jsonToList(hosts);
        return result;
    }

    public long getServerTimeOffset(final long defaultValue) {
        return readLong(SERVER_TIME_OFFSET.key(), defaultValue);
    }

    public long getClientApiLevel(long defValue) {
        return readLong(API_LEVEL.fullKey(), defValue);
    }

    public boolean isDeferredDeeplinkWasChecked() {
        return readBoolean(DEFERRED_DEEP_LINK_WAS_CHECKED.fullKey(), false);
    }

    @NonNull
    public PreferencesClientDbStorage putUuidResult(@Nullable IdentifiersResult uuid) {
        return putAdsIdentifiersResult(UUID_RESULT.fullKey(), uuid);
    }

    @NonNull
    public PreferencesClientDbStorage putDeviceIdResult(@Nullable IdentifiersResult deviceId) {
        return putAdsIdentifiersResult(DEVICE_ID_RESULT.fullKey(), deviceId);
    }

    @NonNull
    public PreferencesClientDbStorage putDeviceIdHashResult(@Nullable IdentifiersResult deviceIdHash) {
        return putAdsIdentifiersResult(DEVICE_ID_HASH_RESULT.fullKey(), deviceIdHash);
    }

    @NonNull
    public PreferencesClientDbStorage putAdUrlGetResult(@Nullable IdentifiersResult adUrlGet) {
        return putAdsIdentifiersResult(AD_URL_GET_RESULT.fullKey(), adUrlGet);
    }

    @NonNull
    public PreferencesClientDbStorage putAdUrlReportResult(@Nullable IdentifiersResult adUrlReport) {
        return putAdsIdentifiersResult(AD_URL_REPORT_RESULT.fullKey(), adUrlReport);
    }

    @NonNull
    public PreferencesClientDbStorage putResponseClidsResult(@Nullable IdentifiersResult clids) {
        return putAdsIdentifiersResult(RESPONSE_CLIDS_RESULT.fullKey(), clids);
    }

    @NonNull
    public PreferencesClientDbStorage putCustomSdkHosts(@Nullable IdentifiersResult customSdkHosts) {
        return putAdsIdentifiersResult(CUSTOM_SDK_HOSTS.fullKey(), customSdkHosts);
    }

    @NonNull
    public PreferencesClientDbStorage putFeatures(@NonNull FeaturesInternal features) {
        return writeString(FEATURES.fullKey(), JsonHelper.featuresToJson(features));
    }

    @NonNull
    public PreferencesClientDbStorage putGaid(@Nullable IdentifiersResult gaid) {
        return putAdsIdentifiersResult(GAID.fullKey(), gaid);
    }

    @NonNull
    public PreferencesClientDbStorage putHoaid(@Nullable IdentifiersResult hoaid) {
        return putAdsIdentifiersResult(HOAID.fullKey(), hoaid);
    }

    @NonNull
    public PreferencesClientDbStorage putYandexAdvId(@Nullable IdentifiersResult hoaid) {
        return putAdsIdentifiersResult(YANDEX_ADV_ID.fullKey(), hoaid);
    }

    public PreferencesClientDbStorage putClientClidsChangedAfterLastIdentifiersUpdate(boolean value) {
        return writeBoolean(CLIENT_CLIDS_CHANGED_AFTER_LAST_IDENTIFIERS_UPDATE.fullKey(), value);
    }

    @NonNull
    public PreferencesClientDbStorage remove(@NonNull String key) {
        return removeKey(new PreferencesItem(key).fullKey());
    }

    public PreferencesClientDbStorage putCustomHosts(final List<String> hosts) {
        String value = JsonHelper.listToJsonString(hosts);
        return writeString(CUSTOM_HOSTS.fullKey(), value);
    }

    public PreferencesClientDbStorage putServerTimeOffset(final long value) {
        return writeLong(SERVER_TIME_OFFSET.fullKey(), value);
    }

    public PreferencesClientDbStorage putClientApiLevel(final long apiLevel) {
        return writeLong(API_LEVEL.fullKey(), apiLevel);
    }

    public PreferencesClientDbStorage markDeferredDeeplinkChecked() {
        return writeBoolean(DEFERRED_DEEP_LINK_WAS_CHECKED.fullKey(), true);
    }

    public PreferencesClientDbStorage putClientClids(@Nullable String clids) {
        return writeString(CLIENT_CLIDS.fullKey(), clids);
    }

    @Nullable
    public String getClientClids(@Nullable String defaultValue) {
        return readString(CLIENT_CLIDS.fullKey(), defaultValue);
    }

    @NonNull
    public PreferencesClientDbStorage putNextStartupTime(final long nextStartupTime) {
        return writeLong(NEXT_STARTUP_TIME.fullKey(), nextStartupTime);
    }

    @NonNull
    public long getNextStartupTime() {
        return readLong(NEXT_STARTUP_TIME.fullKey(), 0);
    }

    @Nullable
    public ScreenInfo getScreenInfo() {
        return JsonHelper.screenInfoFromJsonString(readString(SCREEN_INFO.fullKey()));
    }

    public void saveScreenInfo(@Nullable ScreenInfo screenInfo) {
        writeString(SCREEN_INFO.fullKey(), JsonHelper.screenInfoToJsonString(screenInfo));
    }

    public boolean isScreenSizeCheckedByDeprecated() {
        return readBoolean(SCREEN_SIZE_CHECKED_BY_DEPRECATED.fullKey(), false);
    }

    public void markScreenSizeCheckedByDeprecated() {
        writeBoolean(SCREEN_SIZE_CHECKED_BY_DEPRECATED.fullKey(), true);
    }

    @NonNull
    private IdentifiersResult getAdsIdentifiersResult(@NonNull String key) {
        IdentifiersResult result = null;
        try {
            String savedValue = readString(key, null);
            if (savedValue != null) {
                result = JsonHelper.adsIdentifiersResultFromJson(new JSONObject(savedValue));
            }
        } catch (Throwable e) {
            YLogger.e(e, "%s%s", TAG, e.getMessage());
        }
        return result == null ? new IdentifiersResult(
                null,
                IdentifierStatus.UNKNOWN,
                "no identifier in preferences"
        ) : result;
    }

    private PreferencesClientDbStorage putAdsIdentifiersResult(
            @NonNull String key,
            @Nullable IdentifiersResult identifiersResult
    ) {
        String valueToSave = null;
        if (identifiersResult != null) {
            try {
                valueToSave = JsonHelper.adsIdentifiersResultToJson(identifiersResult).toString();
            } catch (Throwable e) {
                YLogger.e(e, "%s%s", TAG, e.getMessage());
            }
        }

        if (valueToSave != null) {
            writeString(key, valueToSave);
        }
        return this;
    }
}
