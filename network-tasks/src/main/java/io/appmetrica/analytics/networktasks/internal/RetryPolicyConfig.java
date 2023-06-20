package io.appmetrica.analytics.networktasks.internal;

public class RetryPolicyConfig {

    public final int maxIntervalSeconds;
    public final int exponentialMultiplier;

    public RetryPolicyConfig(int maxIntervalSeconds, int exponentialMultiplier) {
        this.maxIntervalSeconds = maxIntervalSeconds;
        this.exponentialMultiplier = exponentialMultiplier;
    }

    @Override
    public String toString() {
        return "RetryPolicyConfig{" +
                "maxIntervalSeconds=" + maxIntervalSeconds +
                ", exponentialMultiplier=" + exponentialMultiplier +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RetryPolicyConfig that = (RetryPolicyConfig) o;

        if (maxIntervalSeconds != that.maxIntervalSeconds) return false;
        return exponentialMultiplier == that.exponentialMultiplier;
    }

    @Override
    public int hashCode() {
        int result = maxIntervalSeconds;
        result = 31 * result + exponentialMultiplier;
        return result;
    }

}
