package io.appmetrica.analytics.networktasks.impl.utils;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeUtils {

    public static int getTimeZoneOffsetSec(long timeSec) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        TimeZone timeZone = cal.getTimeZone();

        return timeZone.getOffset(timeSec * 1000) / 1000;
    }

}
