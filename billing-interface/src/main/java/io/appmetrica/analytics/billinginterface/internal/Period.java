package io.appmetrica.analytics.billinginterface.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Period {

    private static final Pattern PERIOD_PATTERN = Pattern.compile("P(\\d+)(\\S+)");

    public enum TimeUnit {
        TIME_UNIT_UNKNOWN, DAY, WEEK, MONTH, YEAR;
    }

    public final int number;
    @NonNull
    public final Period.TimeUnit timeUnit;

    public Period(final int number,
                  @NonNull final Period.TimeUnit timeUnit) {
        this.number = number;
        this.timeUnit = timeUnit;
    }

    @Nullable
    public static Period parse(@NonNull final String period) {
        final Matcher matcher = PERIOD_PATTERN.matcher(period);
        if (matcher.find()) {
            final String number = matcher.group(1);
            final String timeUnit = matcher.group(2);
            if (number != null && timeUnit != null) {
                try {
                    return new Period(Integer.parseInt(number), toTimeUnit(timeUnit.charAt(0)));
                } catch (Throwable ignore) { }
            }
        }
        return null;
    }

    private static Period.TimeUnit toTimeUnit(final char period) {
        switch (period) {
            case 'D': return Period.TimeUnit.DAY;
            case 'W': return Period.TimeUnit.WEEK;
            case 'M': return Period.TimeUnit.MONTH;
            case 'Y': return Period.TimeUnit.YEAR;
            default: return Period.TimeUnit.TIME_UNIT_UNKNOWN;
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "Period{" +
                "number=" + number +
                "timeUnit=" + timeUnit +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Period that = (Period) o;

        if (number != that.number) return false;
        if (timeUnit != that.timeUnit) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = 31 * hash + number;
        hash = 31 * hash + timeUnit.hashCode();
        return hash;
    }
}
