package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import java.math.BigDecimal;

public class AmountWrapper {

    @NonNull
    public final BigDecimal amount;
    @NonNull
    public final String unit;

    public AmountWrapper(@NonNull ECommerceAmount input) {
        this(input.getAmount(), input.getUnit());
    }

    @VisibleForTesting
    public AmountWrapper(@NonNull BigDecimal amount, @NonNull String unit) {
        this.amount = amount;
        this.unit = unit;
    }

    @NonNull
    @Override
    public String toString() {
        return "AmountWrapper{" +
                "amount=" + amount +
                ", unit='" + unit + '\'' +
                '}';
    }
}
