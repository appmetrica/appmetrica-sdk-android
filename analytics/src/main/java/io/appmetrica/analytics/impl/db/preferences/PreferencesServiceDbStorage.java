package io.appmetrica.analytics.impl.db.preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.VitalDataSource;
import io.appmetrica.analytics.impl.network.NetworkHost;
import java.util.Set;

public class PreferencesServiceDbStorage extends NameSpacedPreferenceDbStorage
    implements VitalDataSource {

    static final PreferencesItem LOCATION_TRACKING_ENABLED = new PreferencesItem("LOCATION_TRACKING_ENABLED");
    static final PreferencesItem SERVER_TIME_OFFSET = new PreferencesItem("PREF_KEY_OFFSET");
    static final PreferencesItem UNCHECKED_TIME = new PreferencesItem("UNCHECKED_TIME");
    static final PreferencesItem DATA_SENDING_RESTRICTED_IN_MAIN = new PreferencesItem("STATISTICS_RESTRICTED_IN_MAIN");
    static final PreferencesItem LAST_IDENTITY_LIGHT_SEND_TIME = new PreferencesItem("LAST_IDENTITY_LIGHT_SEND_TIME");
    static final PreferencesItem NEXT_REPORT_SEND_ATTEMPT_NUMBER =
            new PreferencesItem("NEXT_REPORT_SEND_ATTEMPT_NUMBER");
    static final PreferencesItem NEXT_LOCATION_SEND_ATTEMPT_NUMBER =
            new PreferencesItem("NEXT_LOCATION_SEND_ATTEMPT_NUMBER");
    static final PreferencesItem NEXT_STARTUP_SEND_ATTEMPT_NUMBER =
            new PreferencesItem("NEXT_STARTUP_SEND_ATTEMPT_NUMBER");
    static final PreferencesItem LAST_REPORT_SEND_ATTEMPT_TIME = new PreferencesItem("LAST_REPORT_SEND_ATTEMPT_TIME");
    static final PreferencesItem LAST_LOCATION_SEND_ATTEMPT_TIME =
            new PreferencesItem("LAST_LOCATION_SEND_ATTEMPT_TIME");
    static final PreferencesItem LAST_STARTUP_SEND_ATTEMPT_TIME =
            new PreferencesItem("LAST_STARTUP_SEND_ATTEMPT_TIME");

    static final PreferencesItem SATELLITE_PRELOAD_INFO_CHECKED =
            new PreferencesItem("SATELLITE_PRELOAD_INFO_CHECKED");
    static final PreferencesItem SATELLITE_CLIDS_CHECKED = new PreferencesItem("SATELLITE_CLIDS_CHECKED");

    static final PreferencesItem VITAL_DATA = new PreferencesItem("VITAL_DATA");
    static final PreferencesItem LAST_KOTLIN_VERSION_SEND_TIME = new PreferencesItem("LAST_KOTLIN_VERSION_SEND_TIME");
    static final PreferencesItem ADV_IDENTIFIERS_TRACKING_ENABLED =
        new PreferencesItem("ADV_IDENTIFIERS_TRACKING_ENABLED");

    public PreferencesServiceDbStorage(final IKeyValueTableDbHelper dbStorage) {
        super(dbStorage);
    }

    public void saveLocationTrackingEnabled(boolean enabled) {
        writeBoolean(LOCATION_TRACKING_ENABLED.fullKey(), enabled).commit();
    }

    public boolean isLocationTrackingEnabled() {
        return readBoolean(LOCATION_TRACKING_ENABLED.fullKey(), false);
    }

    public long getServerTimeOffset(final int defaultValue) {
        return readLong(SERVER_TIME_OFFSET.fullKey(), defaultValue);
    }

    public PreferencesServiceDbStorage putServerTimeOffset(final long value) {
        return writeLong(SERVER_TIME_OFFSET.fullKey(), value);
    }

    public boolean isUncheckedTime(final boolean defValue) {
        return readBoolean(UNCHECKED_TIME.fullKey(), defValue);
    }

    public PreferencesServiceDbStorage putUncheckedTime(final boolean value) {
        return writeBoolean(UNCHECKED_TIME.fullKey(), value);
    }

    @Nullable
    public Boolean getDataSendingRestrictedFromMainReporter() {
        return containsKey(DATA_SENDING_RESTRICTED_IN_MAIN.fullKey())
                ? readBoolean(DATA_SENDING_RESTRICTED_IN_MAIN.fullKey(), true)
                : null;
    }

    public PreferencesServiceDbStorage putDataSendingRestrictedFromMainReporter(boolean value) {
        return writeBoolean(DATA_SENDING_RESTRICTED_IN_MAIN.fullKey(), value);
    }

    public long getLastIdentityLightSendTimeSeconds(long defaultValue) {
        return readLong(LAST_IDENTITY_LIGHT_SEND_TIME.fullKey(), defaultValue);
    }

    public PreferencesServiceDbStorage putLastIdentityLightSendTimeSeconds(long value) {
        return writeLong(LAST_IDENTITY_LIGHT_SEND_TIME.fullKey(), value);
    }

    public int getNextSendAttemptNumber(@NonNull NetworkHost host, int defaultValue) {
        PreferencesItem preferencesItem = hostToNextAttemptKey(host);
        return preferencesItem == null ? defaultValue : readInt(preferencesItem.fullKey(), defaultValue);
    }

    public PreferencesServiceDbStorage putNextSendAttemptNumber(@NonNull NetworkHost host, int value) {
        PreferencesItem preferencesItem = hostToNextAttemptKey(host);
        if (preferencesItem != null) {
            return writeInt(preferencesItem.fullKey(), value);
        } else {
            return this;
        }
    }

    public long getLastSendAttemptTimeSeconds(@NonNull NetworkHost host, long defaultValue) {
        PreferencesItem preferencesItem = hostToLastTimeKey(host);
        return preferencesItem == null ? defaultValue : readLong(preferencesItem.fullKey(), defaultValue);
    }

    public PreferencesServiceDbStorage putLastSendAttemptTimeSeconds(@NonNull NetworkHost host, long value) {
        PreferencesItem preferencesItem = hostToLastTimeKey(host);
        if (preferencesItem != null) {
            return writeLong(preferencesItem.fullKey(), value);
        } else {
            return this;
        }
    }

    public boolean wasSatellitePreloadInfoChecked() {
        return readBoolean(SATELLITE_PRELOAD_INFO_CHECKED.fullKey(),false);
    }

    public PreferencesServiceDbStorage markSatellitePreloadInfoChecked() {
        return writeBoolean(SATELLITE_PRELOAD_INFO_CHECKED.fullKey(), true);
    }

    public boolean wereSatelliteClidsChecked() {
        return readBoolean(SATELLITE_CLIDS_CHECKED.fullKey(), false);
    }

    public PreferencesServiceDbStorage markSatelliteClidsChecked() {
        return writeBoolean(SATELLITE_CLIDS_CHECKED.fullKey(), true);
    }

    public long lastKotlinVersionSendTime() {
        return readLong(LAST_KOTLIN_VERSION_SEND_TIME.fullKey(), 0);
    }

    public PreferencesServiceDbStorage putLastKotlinVersionSendTime(long value) {
        return writeLong(LAST_KOTLIN_VERSION_SEND_TIME.fullKey(), value);
    }

    @Nullable
    @Override
    public String getVitalData() {
        return readString(VITAL_DATA.fullKey(), null);
    }

    @Override
    public void putVitalData(@NonNull String data) {
        writeString(VITAL_DATA.fullKey(), data).commit();
    }

    public void saveAdvIdentifiersTrackingEnabled(boolean value) {
        writeBoolean(ADV_IDENTIFIERS_TRACKING_ENABLED.fullKey(), value).commit();
    }

    public boolean isAdvIdentifiersTrackingStatusEnabled(boolean defaultValue) {
        return readBoolean(ADV_IDENTIFIERS_TRACKING_ENABLED.fullKey(), defaultValue);
    }

    @NonNull
    @Override
    protected String prepareKey(@NonNull String key) {
        return new PreferencesItem(key).fullKey();
    }

    @NonNull
    @Override
    public Set<String> keys() {
        return super.keys();
    }

    private PreferencesItem hostToNextAttemptKey(@NonNull NetworkHost host) {
        switch (host) {
            case REPORT:
                return NEXT_REPORT_SEND_ATTEMPT_NUMBER;
            case STARTUP:
                return NEXT_STARTUP_SEND_ATTEMPT_NUMBER;
            case LOCATION:
                return NEXT_LOCATION_SEND_ATTEMPT_NUMBER;
            default:
                return null;
        }
    }

    private PreferencesItem hostToLastTimeKey(@NonNull NetworkHost host) {
        switch (host) {
            case REPORT:
                return LAST_REPORT_SEND_ATTEMPT_TIME;
            case STARTUP:
                return LAST_STARTUP_SEND_ATTEMPT_TIME;
            case LOCATION:
                return LAST_LOCATION_SEND_ATTEMPT_TIME;
            default:
                return null;
        }
    }
}
