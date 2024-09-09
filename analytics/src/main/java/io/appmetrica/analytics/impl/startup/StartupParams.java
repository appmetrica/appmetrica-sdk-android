package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsItem;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StartupParams {

    private static final String TAG = "[StartupParams]";

    private static final String UUID_KEY = Constants.StartupParamsCallbackKeys.UUID;
    private static final String DEVICE_ID_KEY = Constants.StartupParamsCallbackKeys.DEVICE_ID;
    private static final String DEVICE_ID_HASH_KEY = Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH;
    private static final String GET_AD_URL_KEY = Constants.StartupParamsCallbackKeys.GET_AD_URL;
    private static final String REPORT_AD_URL_KEY = Constants.StartupParamsCallbackKeys
            .REPORT_AD_URL;
    private static final String RESPONSE_CLIDS_KEY = Constants.StartupParamsCallbackKeys.CLIDS;
    private static final String GAID_KEY = Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID;
    private static final String HOAID_KEY = Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID;
    private static final String YANDEX_ADV_ID_KEY =
            Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID;

    private final Set<String> advIdentifiersKeys = new HashSet<String>();
    private final Map<String, IdentifiersResult> mIdentifiers =
            new HashMap<String, IdentifiersResult>();
    private final StartupParamItemAdapter startupParamItemAdapter = new StartupParamItemAdapter();
    private List<String> mCustomHosts;
    private Map<String, String> mClientClids;
    private long mServerTimeOffsetSeconds;
    private boolean mClientClidsChangedAfterLastIdentifiersUpdate;
    private long nextStartupTime;

    private final PreferencesClientDbStorage mPreferences;
    @NonNull
    private final AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter;
    @NonNull
    private final ClidsStateChecker clidsStateChecker;
    @NonNull
    private final CustomSdkHostsHolder customSdkHostsHolder;
    @NonNull
    private final FeaturesHolder featuresHolder;
    @NonNull
    private final FeaturesConverter featuresConverter;
    @NonNull
    private final UuidValidator uuidValidator;

    public StartupParams(@NonNull Context context, @NonNull PreferencesClientDbStorage preferencesClientDbStorage) {
        this(
            preferencesClientDbStorage,
            new AdvIdentifiersFromIdentifierResultConverter(),
            new ClidsStateChecker(),
            ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(context),
            new CustomSdkHostsHolder(),
            new FeaturesHolder(),
            new FeaturesConverter(),
            new UuidValidator()
        );
    }

    @VisibleForTesting
    StartupParams(@NonNull final PreferencesClientDbStorage preferences,
                  @NonNull AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter,
                  @NonNull ClidsStateChecker clidsStateChecker,
                  @NonNull MultiProcessSafeUuidProvider multiProcessSafeUuidProvider,
                  @NonNull CustomSdkHostsHolder customSdkHostsHolder,
                  @NonNull FeaturesHolder featuresHolder,
                  @NonNull FeaturesConverter featuresConverter,
                  @NonNull UuidValidator uuidValidator) {
        advIdentifiersKeys.add(GAID_KEY);
        advIdentifiersKeys.add(HOAID_KEY);
        advIdentifiersKeys.add(YANDEX_ADV_ID_KEY);
        mPreferences = preferences;
        this.advIdentifiersConverter = advIdentifiersConverter;
        this.clidsStateChecker = clidsStateChecker;
        this.customSdkHostsHolder = customSdkHostsHolder;
        this.featuresHolder = featuresHolder;
        this.featuresConverter = featuresConverter;
        this.uuidValidator = uuidValidator;

        putUuidIfValid(multiProcessSafeUuidProvider.readUuid());

        putIdentifierIfNotEmpty(DEVICE_ID_KEY, mPreferences.getDeviceIdResult());
        putIdentifierIfNotEmpty(DEVICE_ID_HASH_KEY, mPreferences.getDeviceIdHashResult());
        putIdentifierIfNotEmpty(GET_AD_URL_KEY, mPreferences.getAdUrlGetResult());
        putIdentifierIfNotEmpty(REPORT_AD_URL_KEY, mPreferences.getAdUrlReportResult());
        putIdentifierIfNotNull(RESPONSE_CLIDS_KEY, mPreferences.getResponseClidsResult());
        putIdentifierIfNotEmpty(GAID_KEY, mPreferences.getGaid());
        putIdentifierIfNotEmpty(HOAID_KEY, mPreferences.getHoaid());
        putIdentifierIfNotEmpty(YANDEX_ADV_ID_KEY, mPreferences.getYandexAdvId());
        customSdkHostsHolder.update(mPreferences.getCustomSdkHosts());
        this.featuresHolder.setFeatures(mPreferences.getFeatures());
        this.mCustomHosts = mPreferences.getCustomHosts();
        String clidsString = mPreferences.getClientClids(null);
        mClientClids = clidsString == null ? null : StartupUtils.decodeClids(clidsString);
        mClientClidsChangedAfterLastIdentifiersUpdate = mPreferences
                .getClientClidsChangedAfterLastIdentifiersUpdate(true);
        mServerTimeOffsetSeconds = mPreferences.getServerTimeOffset(0);
        nextStartupTime = mPreferences.getNextStartupTime();

        updateAllParamsPreferences();
    }

    public void setClientClids(@Nullable Map<String, String> clids) {
        if (Utils.isNullOrEmpty(clids) == false && Utils.areEqual(clids, mClientClids) == false) {
            DebugLogger.INSTANCE.info(TAG, "Update client clids from %s to %s", mClientClids, clids);
            mClientClids = new HashMap<String, String>(clids);
            mClientClidsChangedAfterLastIdentifiersUpdate = true;

            updateAllParamsPreferences();
        }

    }

    public boolean areResponseClidsConsistent() {
        IdentifiersResult responseClids = mIdentifiers.get(RESPONSE_CLIDS_KEY);
        if (isIdentifierNull(responseClids)) {
            return true;
        }
        if (responseClids.id.isEmpty()) {
            return Utils.isNullOrEmpty(mClientClids);
        }
        return true;
    }

    private void putIdentifierIfNotEmpty(@NonNull String key, @Nullable IdentifiersResult identifier) {
        if (isIdentifierNullOrEmpty(identifier) == false) {
            mIdentifiers.put(key, identifier);
        }
    }

    private void putUuidIfValid(@Nullable IdentifiersResult identifier) {
        if (isUuidValid(identifier)) {
            mIdentifiers.put(UUID_KEY, identifier);
        }
    }

    private void putIdentifierIfNotNull(@NonNull String key, @Nullable IdentifiersResult identifier) {
        if (isIdentifierNull(identifier) == false) {
            mIdentifiers.put(key, identifier);
        }
    }

    private void initUuid(@Nullable IdentifiersResult uuid) {
        if (!isUuidValid(mIdentifiers.get(UUID_KEY))) {
            putUuidIfValid(uuid);
        }
    }

    private boolean isUuidValid(@Nullable IdentifiersResult identifier) {
        return identifier != null && uuidValidator.isValid(identifier.id);
    }

    private boolean isIdentifierNull(@Nullable IdentifiersResult identifier) {
        return identifier == null || identifier.id == null;
    }

    private boolean isIdentifierNullOrEmpty(@NonNull String key) {
        return isIdentifierNullOrEmpty(mIdentifiers.get(key));
    }

    private boolean isIdentifierNullOrEmpty(@Nullable IdentifiersResult identifier) {
        return identifier == null || TextUtils.isEmpty(identifier.id);
    }

    synchronized void putToMap(@NonNull List<String> params, final Map<String, StartupParamsItem> mapToPut) {
        for (String param : params) {
            IdentifiersResult identifier = mIdentifiers.get(param);
            if (identifier != null) {
                mapToPut.put(param, startupParamItemAdapter.adapt(identifier));
            }
        }
        customSdkHostsHolder.putToMap(params, mapToPut);
        featuresHolder.putToMap(params, mapToPut);
    }

    synchronized boolean shouldSendStartup() {
        return shouldSendStartup(Arrays.asList(
                Constants.StartupParamsCallbackKeys.CLIDS,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                Constants.StartupParamsCallbackKeys.DEVICE_ID,
                Constants.StartupParamsCallbackKeys.GET_AD_URL,
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                Constants.StartupParamsCallbackKeys.UUID
        ));
    }

    synchronized boolean shouldSendStartup(@NonNull List<String> identifiers) {
        boolean notAllIdentifiers = !containsIdentifiers(
                StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(identifiers)
        );
        boolean advIdentifiersRequested = listContainsAdvIdentifiers(identifiers);
        boolean outdated = StartupRequiredUtils.isOutdated(nextStartupTime);
        boolean result = notAllIdentifiers || advIdentifiersRequested || outdated ||
                mClientClidsChangedAfterLastIdentifiersUpdate;

        DebugLogger.INSTANCE.info(
            TAG,
            "shouldSendStartup = %b:  notAllIdentifiers = %b; advIdentifiersRequested = %b, outdated = %b; " +
                "mClientClidsChanged = %b; identifiers = %s",
            result,
            notAllIdentifiers,
            advIdentifiersRequested,
            outdated,
            mClientClidsChangedAfterLastIdentifiersUpdate,
            identifiers
        );

        return result;
    }

    @VisibleForTesting
    boolean listContainsAdvIdentifiers(@NonNull List<String> identifiers) {
        for (String identifier : identifiers) {
            if (advIdentifiersKeys.contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    synchronized boolean containsIdentifiers(@NonNull Collection<String> identifiers) {
        for (String identifier : identifiers) {
            IdentifiersResult savedIdentifier = mIdentifiers.get(identifier);
            if (savedIdentifier == null) {
                savedIdentifier = customSdkHostsHolder.getResultMap().get(identifier);
            }
            if (savedIdentifier == null) {
                savedIdentifier = featuresHolder.getFeature(identifier);
            }
            if (Constants.StartupParamsCallbackKeys.CLIDS.equals(identifier)) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "checking if contains clids. Client clids: %s, response clids: %s, " +
                        "mClientClidsChangedAfterLastIdentifiersUpdate: %b",
                    mClientClids,
                    savedIdentifier,
                    mClientClidsChangedAfterLastIdentifiersUpdate
                );
                if (mClientClidsChangedAfterLastIdentifiersUpdate || isIdentifierNull(savedIdentifier) ||
                        (savedIdentifier.id.isEmpty() && Utils.isNullOrEmpty(mClientClids) == false)) {
                    return false;
                }
            } else if (Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED
                    .equals(identifier)
            ) {
                if (savedIdentifier == null) {
                    return false;
                }
            } else if (isIdentifierNullOrEmpty(savedIdentifier)) {
                return false;
            }
        }
        return true;
    }

    synchronized void updateAllParamsByReceiver(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        updateIdentifiersByReceiver(clientIdentifiersHolder);
        customSdkHostsHolder.update(clientIdentifiersHolder.getCustomSdkHosts());
        featuresHolder.setFeatures(clientIdentifiersHolder.getFeatures());
        updateUrlsByReceiver(clientIdentifiersHolder);
        updateServerTimeOffset(clientIdentifiersHolder);
        updateClidsByReceiver(clientIdentifiersHolder);
        nextStartupTime = clientIdentifiersHolder.getNextStartupTime();

        updateAllParamsPreferences();
    }

    private void updateAllParamsPreferences() {
        mPreferences.putUuidResult(mIdentifiers.get(UUID_KEY))
                .putDeviceIdResult(mIdentifiers.get(DEVICE_ID_KEY))
                .putDeviceIdHashResult(mIdentifiers.get(DEVICE_ID_HASH_KEY))
                .putAdUrlGetResult(mIdentifiers.get(GET_AD_URL_KEY))
                .putAdUrlReportResult(mIdentifiers.get(REPORT_AD_URL_KEY))
                .putServerTimeOffset(mServerTimeOffsetSeconds)
                .putResponseClidsResult(mIdentifiers.get(RESPONSE_CLIDS_KEY))
                .putClientClids(StartupUtils.encodeClids(mClientClids))
                .putGaid(mIdentifiers.get(GAID_KEY))
                .putHoaid(mIdentifiers.get(HOAID_KEY))
                .putYandexAdvId(mIdentifiers.get(YANDEX_ADV_ID_KEY))
                .putClientClidsChangedAfterLastIdentifiersUpdate(mClientClidsChangedAfterLastIdentifiersUpdate)
                .putCustomSdkHosts(customSdkHostsHolder.getCommonResult())
                .putNextStartupTime(nextStartupTime)
                .putFeatures(featuresHolder.getFeatures())
                .commit();
    }

    private void updateClidsByReceiver(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        if (clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                mClientClids,
                JsonHelper.clidsFromString(clientIdentifiersHolder.getClientClidsForRequest().id)
        )) {
            mIdentifiers.put(RESPONSE_CLIDS_KEY, clientIdentifiersHolder.getResponseClids());
            mClientClidsChangedAfterLastIdentifiersUpdate = false;
        }
    }

    List<String> getCustomHosts() {
        return mCustomHosts;
    }

    void setCustomHosts(final List<String> customHosts) {
        mCustomHosts = customHosts;
        mPreferences.putCustomHosts(mCustomHosts);
    }

    private void updateServerTimeOffset(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        setServerTimeOffset(clientIdentifiersHolder.getServerTimeOffset());
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Nullable
    public String getDeviceIDHash() {
        return getIdentifierOrNull(DEVICE_ID_HASH_KEY);
    }

    @NonNull
    public FeaturesResult getFeatures() {
        return featuresConverter.convert(featuresHolder.getFeatures());
    }

    private void updateIdentifiersByReceiver(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        initUuid(clientIdentifiersHolder.getUuid());
        putIdentifierIfNotEmpty(DEVICE_ID_KEY, clientIdentifiersHolder.getDeviceId());
        putIdentifierIfNotEmpty(DEVICE_ID_HASH_KEY, clientIdentifiersHolder.getDeviceIdHash());
        mIdentifiers.put(GAID_KEY, clientIdentifiersHolder.getGaid());
        mIdentifiers.put(HOAID_KEY, clientIdentifiersHolder.getHoaid());
        mIdentifiers.put(YANDEX_ADV_ID_KEY, clientIdentifiersHolder.getYandexAdvId());
    }

    private void updateUrlsByReceiver(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        IdentifiersResult receivedAdUrlGet = clientIdentifiersHolder.getGetAdUrl();
        if (isIdentifierNull(receivedAdUrlGet) == false) {
            setAdUrlGet(receivedAdUrlGet);
        }
        IdentifiersResult receivedAdUrlReport = clientIdentifiersHolder.getReportAdUrl();
        if (isIdentifierNull(receivedAdUrlReport) == false) {
            setAdUrlReport(receivedAdUrlReport);
        }
    }

    @VisibleForTesting
    void setDeviceId(final IdentifiersResult deviceId) {
        mIdentifiers.put(DEVICE_ID_KEY, deviceId);
    }

    private void setAdUrlGet(final IdentifiersResult adUrlGet) {
        mIdentifiers.put(GET_AD_URL_KEY, adUrlGet);
    }

    private void setAdUrlReport(final IdentifiersResult adUrlReport) {
        mIdentifiers.put(REPORT_AD_URL_KEY, adUrlReport);
    }

    private void setServerTimeOffset(final long serverTimeOffset) {
        mServerTimeOffsetSeconds = serverTimeOffset;
    }

    @Nullable
    String getUuid() {
        return getIdentifierOrNull(UUID_KEY);
    }

    @Nullable
    String getDeviceId() {
        return getIdentifierOrNull(DEVICE_ID_KEY);
    }

    long getServerTimeOffsetSeconds() {
        return mServerTimeOffsetSeconds;
    }

    @Nullable
    String getClids() {
        return getIdentifierOrNull(RESPONSE_CLIDS_KEY);
    }

    @VisibleForTesting
    Map<String, String> getClientClids() {
        return mClientClids;
    }

    private String getIdentifierOrNull(@NonNull String key) {
        IdentifiersResult result = mIdentifiers.get(key);
        return result == null ? null : result.id;
    }

    @NonNull
    public AdvIdentifiersResult getCachedAdvIdentifiers() {
        return advIdentifiersConverter.convert(
                mIdentifiers.get(GAID_KEY), mIdentifiers.get(HOAID_KEY), mIdentifiers.get(YANDEX_ADV_ID_KEY)
        );
    }
}
