package io.appmetrica.analytics.impl.startup;

public class CacheControl {

    public final long lastKnownLocationTtl;

    public CacheControl(long lastKnownLocationTtl) {
        this.lastKnownLocationTtl = lastKnownLocationTtl;
    }

    @Override
    public String toString() {
        return "CacheControl{" +
            "lastKnownLocationTtl=" + lastKnownLocationTtl +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheControl that = (CacheControl) o;

        return lastKnownLocationTtl == that.lastKnownLocationTtl;
    }

    @Override
    public int hashCode() {
        return (int) (lastKnownLocationTtl ^ (lastKnownLocationTtl >>> 32));
    }
}
