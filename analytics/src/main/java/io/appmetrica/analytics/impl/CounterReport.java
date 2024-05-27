package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.billing.ProductInfoWrapper;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ALIVE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_FEATURES;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_UPDATE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_FIRST_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_INIT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PERMISSIONS;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START;

public class CounterReport implements CounterReportApi, Parcelable {

    private static final String TAG = "[CounterReport]";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final Bundle reportData = new Bundle();

        reportData.putString(CounterReportBundleKeys.EVENT, name);
        reportData.putString(CounterReportBundleKeys.VALUE, value);
        reportData.putInt(CounterReportBundleKeys.TYPE, type);
        reportData.putInt(CounterReportBundleKeys.CUSTOM_TYPE, customType);
        reportData.putInt(CounterReportBundleKeys.TRUNCATED, bytesTruncated);
        reportData.putString(CounterReportBundleKeys.PROFILE_ID, profileID);
        reportData.putInt(CounterReportBundleKeys.UNIQUENESS_STATUS, firstOccurrenceStatus.mStatusCode);

        if (payload != null) {
            reportData.putParcelable(CounterReportBundleKeys.PAYLOAD, payload);
        }

        if (null != eventEnvironment) {
            reportData.putString(CounterReportBundleKeys.ENVIRONMENT, eventEnvironment);
        }
        if (appEnvironmentDiff != null) {
            putAppEnvironmentToBundle(reportData, appEnvironmentDiff);
        }
        reportData.putLong(CounterReportBundleKeys.CREATION_ELAPSED_REALTIME, creationElapsedRealtime);
        reportData.putLong(CounterReportBundleKeys.CREATION_TIMESTAMP, creationTimestamp);
        if (source != null) {
            reportData.putInt(CounterReportBundleKeys.SOURCE, source.code);
        }
        if (attributionIdChanged != null) {
            reportData.putBoolean(CounterReportBundleKeys.ATTRIBUTION_ID_CHANGED, attributionIdChanged);
        }
        if (openId != null) {
            reportData.putInt(CounterReportBundleKeys.OPEN_ID, openId);
        }
        reportData.putBundle(CounterReportBundleKeys.EXTRAS, CollectionUtils.mapToBundle(extras));

        dest.writeBundle(reportData);
    }

    public static final Parcelable.Creator<CounterReport> CREATOR =
        new Parcelable.Creator<CounterReport>() {

            public CounterReport createFromParcel(Parcel srcObj) {
                Bundle data = srcObj.readBundle(DataResultReceiver.class.getClassLoader());
                EventSource eventSource = null;
                if (data.containsKey(CounterReportBundleKeys.SOURCE)) {
                    eventSource = EventSource.fromCode(data.getInt(CounterReportBundleKeys.SOURCE));
                }
                CounterReport result = new CounterReport();
                result.setType(data.getInt(CounterReportBundleKeys.TYPE,
                    InternalEvents.EVENT_TYPE_UNDEFINED.getTypeId()));
                result.setCustomType(data.getInt(CounterReportBundleKeys.CUSTOM_TYPE));
                result.setValue(StringUtils.ifIsNullToDef(data.getString(CounterReportBundleKeys.VALUE),
                    StringUtils.EMPTY));
                result.setEventEnvironment(data.getString(CounterReportBundleKeys.ENVIRONMENT));
                result.setName(data.getString(CounterReportBundleKeys.EVENT));
                result.setAppEnvironmentDiff(readAppEnvironmentDiff(data));
                result.setBytesTruncated(data.getInt(CounterReportBundleKeys.TRUNCATED));
                result.setProfileID(data.getString(CounterReportBundleKeys.PROFILE_ID));
                result.setCreationEllapsedRealtime(data.getLong(
                    CounterReportBundleKeys.CREATION_ELAPSED_REALTIME));
                result.setCreationTimestamp(data.getLong(CounterReportBundleKeys.CREATION_TIMESTAMP));
                result.setFirstOccurrenceStatus(FirstOccurrenceStatus.fromStatusCode(
                    data.getInt(CounterReportBundleKeys.UNIQUENESS_STATUS)));
                result.setSource(eventSource);
                result.setPayload(data.getBundle(CounterReportBundleKeys.PAYLOAD));
                result.setAttributionIdChanged(
                    Utils.getBooleanOrNull(data, CounterReportBundleKeys.ATTRIBUTION_ID_CHANGED)
                );
                result.setOpenId(Utils.getIntOrNull(data, CounterReportBundleKeys.OPEN_ID));
                result.setExtras(CollectionUtils.bundleToMap(data.getBundle(CounterReportBundleKeys.EXTRAS)));
                return result;
            }

            public CounterReport[] newArray(int size) {
                return new CounterReport[size];
            }

        };

    @Nullable
    protected String name;
    @Nullable
    protected String value;
    @Nullable
    private String eventEnvironment;
    private int type;
    private int customType;
    @Nullable
    private Pair<String, String> appEnvironmentDiff;
    private int bytesTruncated;
    @Nullable
    private String profileID;
    private long creationElapsedRealtime;
    private long creationTimestamp;
    @NonNull
    private FirstOccurrenceStatus firstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN;
    @Nullable
    private EventSource source;
    @Nullable
    private Bundle payload;
    @Nullable
    private Boolean attributionIdChanged;
    @Nullable
    private Integer openId;
    @NonNull
    private Map<String, byte[]> extras = new HashMap<>();

    public CounterReport() {
        this(StringUtils.EMPTY, 0);
    }

    public CounterReport(@Nullable String event, int type) {
        this(StringUtils.EMPTY, event, type);
    }

    public CounterReport(@Nullable String value, @Nullable String event, final int type) {
        this(value, event, type, new SystemTimeProvider());
    }

    @VisibleForTesting
    public CounterReport(@Nullable String value, @Nullable String event, int type,
                         @NonNull SystemTimeProvider systemTimeProvider) {
        name = event;
        this.type = type;
        this.value = value;
        creationElapsedRealtime = systemTimeProvider.elapsedRealtime();
        creationTimestamp = systemTimeProvider.currentTimeMillis();
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    @Nullable
    public String getValue() {
        return value;
    }

    @Override
    @Nullable
    public byte[] getValueBytes() {
        return value == null ? null : Base64.decode(value, Base64.DEFAULT);
    }

    @Override
    public void setValue(@Nullable String value) {
        this.value = value;
    }

    @Override
    public void setValueBytes(@Nullable byte[] bytes) {
        value = bytes == null ? null : new String(Base64.encode(bytes, Base64.DEFAULT));
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(final int type) {
        this.type = type;
    }

    @Override
    public int getCustomType() {
        return customType;
    }

    @Override
    public void setCustomType(final int customType) {
        this.customType = customType;
    }

    @Nullable
    public Bundle getPayload() {
        return payload;
    }

    @Nullable
    public String getEventEnvironment() {
        return eventEnvironment;
    }

    @Nullable
    public Pair<String, String> getAppEnvironment() {
        return appEnvironmentDiff;
    }

    public void setEventEnvironment(@Nullable String environment) {
        eventEnvironment = environment;
    }

    void setAppEnvironment(@NonNull String key, @Nullable String value) {
        if (appEnvironmentDiff == null) {
            appEnvironmentDiff = new Pair<>(key, value);
        }
    }

    private void setAppEnvironmentDiff(@Nullable Pair<String, String> diff) {
        appEnvironmentDiff = diff;
    }

    @Override
    public void setBytesTruncated(int bytesTruncated) {
        this.bytesTruncated = bytesTruncated;
    }

    protected void setCreationEllapsedRealtime(long creationEllapsedRealtime) {
        creationElapsedRealtime = creationEllapsedRealtime;
    }

    protected void setCreationTimestamp(long creationCurrentTime) {
        creationTimestamp = creationCurrentTime;
    }

    protected void setPayload(@Nullable Bundle payload) {
        this.payload = payload;
    }

    public boolean isNoEvent() {
        return null == name;
    }

    public boolean isUndefinedType() {
        return InternalEvents.EVENT_TYPE_UNDEFINED.getTypeId() == type;
    }

    @Override
    public int getBytesTruncated() {
        return bytesTruncated;
    }

    @Nullable
    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(@Nullable String profileID) {
        this.profileID = profileID;
    }

    @NonNull
    public FirstOccurrenceStatus getFirstOccurrenceStatus() {
        return firstOccurrenceStatus;
    }

    public void setFirstOccurrenceStatus(@NonNull FirstOccurrenceStatus firstOccurrenceStatus) {
        this.firstOccurrenceStatus = firstOccurrenceStatus;
    }

    @Nullable
    public EventSource getSource() {
        return source;
    }

    public void setSource(@Nullable EventSource value) {
        source = value;
    }

    @Nullable
    public Boolean getAttributionIdChanged() {
        return attributionIdChanged;
    }

    public void setAttributionIdChanged(@Nullable Boolean attributionIdChanged) {
        this.attributionIdChanged = attributionIdChanged;
    }

    @Nullable
    public Integer getOpenId() {
        return openId;
    }

    public void setOpenId(@Nullable Integer openId) {
        this.openId = openId;
    }

    @Override
    @NonNull
    public Map<String, byte[]> getExtras() {
        return extras;
    }

    @Override
    public void setExtras(@NonNull Map<String, byte[]> extras) {
        this.extras = extras;
    }

    public long getCreationElapsedRealtime() {
        return creationElapsedRealtime;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    @NonNull
    public Bundle toBundle(final Bundle bundle) {
        final Bundle data = null != bundle ? bundle : new Bundle();
        data.putParcelable(CounterReportBundleKeys.OBJECT, this);
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            Locale.US,
            "[event: %s, type: %s, value: %s]",
            name,
            InternalEvents.valueOf(type).getInfo(),
            Utils.trimToSize(value, Limits.EVENT_VALUE_FOR_LOGS_LIMIT)
        );
    }

    //region Static helpers

    private static void putAppEnvironmentToBundle(@NonNull Bundle data, @NonNull Pair<String, String> environment) {
        data.putString(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_KEY, environment.first);
        data.putString(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_VALUE, environment.second);
    }

    @Nullable
    private static Pair<String, String> readAppEnvironmentDiff(@NonNull Bundle data) {
        if (data.containsKey(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_KEY) &&
            data.containsKey(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_VALUE)) {
            String key = data.getString(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_KEY);
            String value = data.getString(CounterReportBundleKeys.APP_ENVIRONMENT_DIFF_VALUE);
            return new Pair<>(key, value);
        } else {
            return null;
        }
    }

    @NonNull
    public static CounterReport fromBundle(@Nullable Bundle bundle) {
        if (null != bundle) {
            try {
                CounterReport report = bundle.getParcelable(CounterReportBundleKeys.OBJECT);
                if (report != null) {
                    return report;
                }
            } catch (Throwable error) {
                return new CounterReport();
            }
        }
        return new CounterReport();
    }

    @NonNull
    private static CounterReport formReportCopyingMetaDataWithType(@NonNull CounterReport reportData,
                                                                   @NonNull InternalEvents event) {
        final CounterReport resultData = formReportCopyingMetadata(reportData);
        resultData.setType(event.getTypeId());
        return resultData;
    }

    @NonNull
    public static CounterReport formReportCopyingMetadata(@NonNull CounterReport reportData) {
        CounterReport counterReport = new CounterReport();
        counterReport.setCreationTimestamp(reportData.getCreationTimestamp());
        counterReport.setCreationEllapsedRealtime(reportData.getCreationElapsedRealtime());
        counterReport.setAppEnvironmentDiff(reportData.getAppEnvironment());
        counterReport.setEventEnvironment(reportData.getEventEnvironment());
        counterReport.setPayload(reportData.getPayload());
        counterReport.setExtras(reportData.extras);
        counterReport.setProfileID(reportData.getProfileID());
        return counterReport;
    }

    @NonNull
    public static CounterReport formAliveReportData(@NonNull CounterReport reportData) {
        return formReportCopyingMetaDataWithType(reportData, EVENT_TYPE_ALIVE);
    }

    @NonNull
    public static CounterReport formSessionStartReportData(@NonNull CounterReport reportData,
                                                           @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        final CounterReport startReport = formReportCopyingMetaDataWithType(reportData, EVENT_TYPE_START);
        EventStart eventStart = new EventStart(extraMetaInfoRetriever.getBuildId());
        startReport.setValueBytes(MessageNano.toByteArray(new EventStartConverter().fromModel(eventStart)));
        startReport.setCreationTimestamp(reportData.getCreationTimestamp());
        startReport.setCreationEllapsedRealtime(reportData.creationElapsedRealtime);
        return startReport;

    }

    @NonNull
    public static CounterReport formInitReportData(@NonNull CounterReport reportData) {
        return formReportCopyingMetaDataWithType(reportData, EVENT_TYPE_INIT);
    }

    @NonNull
    public static CounterReport formPermissionsReportData(@NonNull CounterReport report,
                                                          @NonNull Collection<PermissionState> newPermissions,
                                                          @Nullable BackgroundRestrictionsState bgRestrictionsState,
                                                          @NonNull AppStandbyBucketConverter converter,
                                                          @NonNull List<String> availableProviders) {
        CounterReport resultData = formReportCopyingMetadata(report);
        String value = StringUtils.EMPTY;
        try {
            JSONArray permissions = new JSONArray();
            for (PermissionState state : newPermissions) {
                permissions.put(new JSONObject().put("name", state.name).put("granted", state.granted));
            }
            JSONObject backgroundRestrictions = new JSONObject();
            if (bgRestrictionsState != null) {
                backgroundRestrictions.put("background_restricted", bgRestrictionsState.mBackgroundRestricted);
                backgroundRestrictions.put("app_standby_bucket",
                    converter.fromAppStandbyBucketToString(bgRestrictionsState.mAppStandByBucket));
            }

            value = new JSONObject()
                .put("permissions", permissions)
                .put("background_restrictions", backgroundRestrictions)
                .put("available_providers", new JSONArray(availableProviders))
                .toString();
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, "error while forming permissions value");
        }
        resultData.setType(EVENT_TYPE_PERMISSIONS.getTypeId());
        resultData.setValue(value);
        return resultData;
    }

    @NonNull
    public static CounterReport formFeaturesReportData(@NonNull CounterReport report,
                                                       @Nullable String value) {
        CounterReport resultData = formReportCopyingMetadata(report);
        resultData.setType(EVENT_TYPE_APP_FEATURES.getTypeId());
        resultData.setValue(value);
        return resultData;
    }

    @NonNull
    public static CounterReport formFirstEventReportData(@NonNull CounterReport reportData) {
        return formReportCopyingMetaDataWithType(reportData, EVENT_TYPE_FIRST_ACTIVATION);
    }

    @NonNull
    public static CounterReport formUpdateReportData(@NonNull CounterReport reportData) {
        return formReportCopyingMetaDataWithType(reportData, EVENT_TYPE_APP_UPDATE);
    }

    @NonNull
    public static CounterReport formUpdatePreActivationConfig() {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.getTypeId());
        return counterReport;
    }

    @NonNull
    public static CounterReport formAutoInappEvent(
        @NonNull final ProductInfoWrapper productInfoWrapper
    ) {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.getTypeId());
        counterReport.setValueBytes(productInfoWrapper.getDataToSend());
        return counterReport;
    }

    @NonNull
    public static CounterReport formJsInitEvent(@NonNull String value) {
        CounterReport result = new CounterReport();
        result.setType(InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.getTypeId());
        result.setValue(value);
        result.setSource(EventSource.JS);
        return result;
    }

    //endregion Static helpers
}
