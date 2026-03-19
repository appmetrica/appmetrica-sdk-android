package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SessionStorageImpl implements SessionStorage {

    private static final String TAG = "[SessionStorageImpl]";

    public static final String SLEEP_START = "SESSION_SLEEP_START";
    public static final String LAST_EVENT_OFFSET = "SESSION_LAST_EVENT_OFFSET";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String REPORT_ID = "SESSION_COUNTER_ID";
    public static final String CREATION_TIME = "SESSION_INIT_TIME";
    public static final String CREATION_CURRENT_TIME = "SESSION_CREATION_CURRENT_TIME";
    public static final String ALIVE_REPORT_NEED = "SESSION_IS_ALIVE_REPORT_NEEDED";
    public static final String CRASHED = "SESSION_CRASHED";

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
            if (!StringUtils.isNullOrEmpty(session)) {
                jsonObject = new JsonHelper.OptJSONObject(session);
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, "can't read %s session description", mSessionTag);
        }
        mJSONObject = jsonObject;
    }

    @Nullable public Long getSessionId() {
        return mJSONObject.getLongSilently(SESSION_ID);
    }

    @Override
    public SessionStorageImpl putSessionId(final long value) {
        putSilently(SESSION_ID, value);
        return this;
    }

    @Nullable public Long getCreationTime() {
        return mJSONObject.getLongSilently(CREATION_TIME);
    }

    @Override
    public SessionStorageImpl putCreationTime(final long value) {
        putSilently(CREATION_TIME, value);
        return this;
    }

    @Nullable public Long getCreationCurrentTimeMillis() {
        return mJSONObject.getLongSilently(CREATION_CURRENT_TIME);
    }

    @Override
    public SessionStorageImpl putCreationCurrentTimeMillis(final long value) {
        putSilently(CREATION_CURRENT_TIME, value);
        return this;
    }

    @Nullable public Long getReportId() {
        return mJSONObject.getLongSilently(REPORT_ID);
    }

    @Override
    public SessionStorageImpl putReportId(final long value) {
        putSilently(REPORT_ID, value);
        return this;
    }

    @Nullable public Long getSleepStart() {
        return mJSONObject.getLongSilently(SLEEP_START);
    }

    @Override
    public SessionStorageImpl putSleepStart(final long value) {
        putSilently(SLEEP_START, value);
        return this;
    }

    @Nullable public Long getLastEventOffset() {
        return mJSONObject.getLongSilently(LAST_EVENT_OFFSET);
    }

    @Override
    public SessionStorageImpl putLastEventOffset(long value) {
        putSilently(LAST_EVENT_OFFSET, value);
        return this;
    }

    @Nullable public Boolean isAliveReportNeeded() {
        return mJSONObject.getBooleanSilently(ALIVE_REPORT_NEED);
    }

    @Override
    public SessionStorageImpl putAliveReportNeeded(final boolean value) {
        putSilently(ALIVE_REPORT_NEED, value);
        return this;
    }

    @Override
    public SessionStorageImpl putCrashedSession(boolean value) {
        putSilently(CRASHED, value);
        return this;
    }

    @Nullable public Boolean isCrashedSession() {
        return mJSONObject.getBooleanSilently(CRASHED);
    }

    @Override
    public void apply() {
        DebugLogger.INSTANCE.info(TAG, "commit: %s", mJSONObject);
        preferences.putSessionParameters(mSessionTag, mJSONObject.toString());
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
        DebugLogger.INSTANCE.info(TAG, "clear: %s", mJSONObject);
        mJSONObject = new JsonHelper.OptJSONObject();
        apply();
    }
}
