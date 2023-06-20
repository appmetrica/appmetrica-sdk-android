package io.appmetrica.analytics.impl.utils;

import android.os.SystemClock;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class TimeUtils {

    private TimeUtils() { }

    public static final long bootTimeMillis() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    /**
     * @return the current time in seconds since January 1, 1970 00:00:00.0 UTC, based on device time.
     */
    public static long currentDeviceTimeSec() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * @return the current time in seconds since January 1, 1970 00:00:00.0 UTC, based on server time.
     */
    public static long currentServerTimeSec() {
        return ServerTime.getInstance().currentTimeSec();
    }

    public static long getServerTimeOffset() {
        return ServerTime.getInstance().getServerTimeOffsetSeconds();
    }

    public static boolean isUncheckedTime() {
        return ServerTime.getInstance().isUncheckedTime();
    }

    public static int getTimeZoneOffsetSec(long timeSec) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        TimeZone timeZone = cal.getTimeZone();

        return timeZone.getOffset(timeSec * 1000) / 1000;
    }

    public static long getElapsedTimeSeconds() {
        return SystemClock.elapsedRealtime() / 1000;
    }

}
