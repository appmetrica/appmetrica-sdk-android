package io.appmetrica.analytics.impl.component.session;

import java.util.concurrent.TimeUnit;

public final class SessionDefaults {
    public static final long INVALID_UI_SESSION_ID = -1;
    public static final long MIN_VALID_UI_SESSION_ID = 0;
    public static final long INITIAL_REPORT_ID = 0;
    public static final long INITIAL_SESSION_TIME = 0;
    public static final long REGULAR_EVENT_NOT_SENT = 0;

    public static final long SESSION_MAX_LENGTH_SEC = TimeUnit.DAYS.toSeconds(1);
}
