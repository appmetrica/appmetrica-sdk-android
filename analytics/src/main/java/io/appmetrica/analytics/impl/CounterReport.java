package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CounterReport implements CounterReportApi, Parcelable {

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

        if (payload != null) {
            reportData.putParcelable(CounterReportBundleKeys.PAYLOAD, payload);
        }

        if (null != eventEnvironment) {
            reportData.putString(CounterReportBundleKeys.ENVIRONMENT, eventEnvironment);
        }
        reportData.putLong(CounterReportBundleKeys.CREATION_ELAPSED_REALTIME, creationElapsedRealtime);
        reportData.putLong(CounterReportBundleKeys.CREATION_TIMESTAMP, creationTimestamp);
        if (source != null) {
            reportData.putInt(CounterReportBundleKeys.SOURCE, source.code);
        }
        reportData.putBundle(CounterReportBundleKeys.EXTRAS, CollectionUtils.mapToBundle(extras));
        if (valueProtocolVersion != null) {
            reportData.putInt(CounterReportBundleKeys.VALUE_PROTOCOL_VERSION, valueProtocolVersion);
        }

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
                result.setBytesTruncated(data.getInt(CounterReportBundleKeys.TRUNCATED));
                result.setProfileID(data.getString(CounterReportBundleKeys.PROFILE_ID));
                result.setCreationEllapsedRealtime(data.getLong(
                    CounterReportBundleKeys.CREATION_ELAPSED_REALTIME));
                result.setCreationTimestamp(data.getLong(CounterReportBundleKeys.CREATION_TIMESTAMP));
                result.setSource(eventSource);
                result.setPayload(data.getBundle(CounterReportBundleKeys.PAYLOAD));
                result.setExtras(CollectionUtils.bundleToMap(data.getBundle(CounterReportBundleKeys.EXTRAS)));
                result.setValueProtocolVersion(
                    Utils.getIntOrNull(data, CounterReportBundleKeys.VALUE_PROTOCOL_VERSION)
                );
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
    private int bytesTruncated;
    @Nullable
    private String profileID;
    private long creationElapsedRealtime;
    private long creationTimestamp;
    @Nullable
    private EventSource source;
    @Nullable
    private Bundle payload;
    @NonNull
    private Map<String, byte[]> extras = new HashMap<>();
    @Nullable
    private Integer valueProtocolVersion;
    @NonNull
    private final SystemTimeProvider systemTimeProvider = new SystemTimeProvider();

    public CounterReport() {
        this(StringUtils.EMPTY, 0);
    }

    public CounterReport(@Nullable String event, int type) {
        this(StringUtils.EMPTY, event, type);
    }

    public CounterReport(@Nullable String value, @Nullable String event, final int type) {
        name = event;
        this.type = type;
        this.value = value;
        creationElapsedRealtime = systemTimeProvider.elapsedRealtime();
        creationTimestamp = systemTimeProvider.currentTimeMillis();
    }

    public CounterReport(@Nullable String value, @Nullable String event, int type, long creationTimestamp) {
        this(value, event, type);
        setCreationTimestamp(creationTimestamp);
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

    public void setEventEnvironment(@Nullable String environment) {
        eventEnvironment = environment;
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

    @Nullable
    public EventSource getSource() {
        return source;
    }

    public void setSource(@Nullable EventSource value) {
        source = value;
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

    @Nullable
    public Integer getValueProtocolVersion() {
        return valueProtocolVersion;
    }

    public void setValueProtocolVersion(@Nullable Integer valueProtocolVersion) {
        this.valueProtocolVersion = valueProtocolVersion;
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
    public static CounterReport formUpdatePreActivationConfig() {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.getTypeId());
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
