package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CounterReport implements CounterReportApi {

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
