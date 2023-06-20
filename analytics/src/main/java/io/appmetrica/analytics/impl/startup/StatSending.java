package io.appmetrica.analytics.impl.startup;

public class StatSending {

    public final long disabledReportingInterval;

    public StatSending(long disabledReportingInterval) {
        this.disabledReportingInterval = disabledReportingInterval;
    }

    @Override
    public String toString() {
        return "StatSending{" +
                "disabledReportingInterval=" + disabledReportingInterval +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StatSending that = (StatSending) o;

        return disabledReportingInterval == that.disabledReportingInterval;
    }

    @Override
    public int hashCode() {
        return (int) (disabledReportingInterval ^ (disabledReportingInterval >>> 32));
    }
}
