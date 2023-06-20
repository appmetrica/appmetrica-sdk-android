package io.appmetrica.analytics.impl.component.session;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.JsonHelper;

public class SessionStorageImpl implements SessionStorage {

    public static final String SLEEP_START = "SESSION_SLEEP_START";
    public static final String LAST_EVENT_OFFSET = "SESSION_LAST_EVENT_OFFSET";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String REPORT_ID = "SESSION_COUNTER_ID";
    public static final String CREATION_TIME = "SESSION_INIT_TIME";
    public static final String ALIVE_REPORT_NEED = "SESSION_IS_ALIVE_REPORT_NEEDED";

    @NonNull
    private final String mSessionTag;

    @NonNull
    protected final PreferencesComponentDbStorage preferences;

    @NonNull
    private JsonHelper.OptJSONObject mJSONObject;

    public SessionStorageImpl(@NonNull PreferencesComponentDbStorage preferences,
                              @NonNull String sessionTag) {
        this.preferences = preferences;
        mSessionTag = sessionTag;
        JsonHelper.OptJSONObject jsonObject = new JsonHelper.OptJSONObject();
        try {
            String session = preferences.getSessionParameters(mSessionTag);
            if (TextUtils.isEmpty(session) == false) {
                jsonObject = new JsonHelper.OptJSONObject(session);
            }
        } catch (Throwable e) {
            YLogger.e(e, "can't read %s session description", mSessionTag);
        }
        mJSONObject = jsonObject;
    }

    @Nullable public Long getSessionId() {
        return mJSONObject.getLongSilently(SESSION_ID);
    }

    public SessionStorageImpl putSessionId(final long value) {
        putSilently(SESSION_ID, value);
        return this;
    }

    @Nullable public Long getCreationTime() {
        return mJSONObject.getLongSilently(CREATION_TIME);
    }

    public SessionStorageImpl putCreationTime(final long value) {
        putSilently(CREATION_TIME, value);
        return this;
    }

    @Nullable public Long getReportId() {
        return mJSONObject.getLongSilently(REPORT_ID);
    }

    public SessionStorageImpl putReportId(final long value) {
        putSilently(REPORT_ID, value);
        return this;
    }

    @Nullable public Long getSleepStart() {
        return mJSONObject.getLongSilently(SLEEP_START);
    }

    public SessionStorageImpl putSleepStart(final long value) {
        putSilently(SLEEP_START, value);
        return this;
    }

    @Nullable public Long getLastEventOffset() {
        return mJSONObject.getLongSilently(LAST_EVENT_OFFSET);
    }

    public SessionStorageImpl putLastEventOffset(long value) {
        putSilently(LAST_EVENT_OFFSET, value);
        return this;
    }

    @Nullable public Boolean isAliveReportNeeded() {
        return mJSONObject.getBooleanSilently(ALIVE_REPORT_NEED);
    }

    public SessionStorageImpl putAliveReportNeeded(final boolean value) {
        putSilently(ALIVE_REPORT_NEED, value);
        return this;
    }

    public void commit() {
        preferences.putSessionParameters(mSessionTag, mJSONObject.toString());
        preferences.commit();
    }

    public boolean hasValues() {
        return mJSONObject.length() > 0;
    }

    private void putSilently(String key, Object value) {
        try {
            mJSONObject.put(key, value);
        } catch (Throwable ignored) {}
    }

    public void clear() {
        mJSONObject = new JsonHelper.OptJSONObject();
        commit();
    }
}
