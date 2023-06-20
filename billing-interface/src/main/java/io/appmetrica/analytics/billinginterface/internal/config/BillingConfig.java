package io.appmetrica.analytics.billinginterface.internal.config;

import androidx.annotation.NonNull;

public class BillingConfig {

    public final int sendFrequencySeconds;
    public final int firstCollectingInappMaxAgeSeconds;

    public BillingConfig(final int sendFrequencySeconds,
                         final int firstCollectingInappMaxAgeSeconds) {
        this.sendFrequencySeconds = sendFrequencySeconds;
        this.firstCollectingInappMaxAgeSeconds = firstCollectingInappMaxAgeSeconds;
    }

    @NonNull
    @Override
    public String toString() {
        return "BillingConfig{" +
                "sendFrequencySeconds=" + sendFrequencySeconds +
                ", firstCollectingInappMaxAgeSeconds=" + firstCollectingInappMaxAgeSeconds +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BillingConfig that = (BillingConfig) o;

        if (sendFrequencySeconds != that.sendFrequencySeconds) return false;
        if (firstCollectingInappMaxAgeSeconds != that.firstCollectingInappMaxAgeSeconds) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sendFrequencySeconds;
        result = 31 * result + firstCollectingInappMaxAgeSeconds;
        return result;
    }
}
