package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.Utils;
import java.math.BigDecimal;

/**
 * Describes an amount of something - number and unit.
 */
public class ECommerceAmount {

    @NonNull
    private final BigDecimal amount;
    @NonNull
    private final String unit;

    /**
     * Creates an amount with its value in micros.
     * @see ECommerceAmount#ECommerceAmount(double, String)
     * @see ECommerceAmount#ECommerceAmount(BigDecimal, String)
     *
     * @param amountMicros Amount value in micros (actual amount multiplied by 10^6).
     * @param unit Amount unit. For example, "USD" "RUB", etc.
     */
    public ECommerceAmount(long amountMicros, @NonNull String unit) {
        this(Utils.microsToBigDecimal(amountMicros), unit);
    }

    /**
     * Creates an amount with double value.
     * @see ECommerceAmount#ECommerceAmount(long, String)
     * @see ECommerceAmount#ECommerceAmount(BigDecimal, String)
     * *
     * @param amount Amount value as double.
     *               {@link java.lang.Double#POSITIVE_INFINITY}, {@link java.lang.Double#NEGATIVE_INFINITY}
     *               and {@link java.lang.Double#NaN} will be treated as 0.
     * @param unit Amount unit. For example, "USD", "RUB", etc.
     */
    public ECommerceAmount(double amount, @NonNull String unit) {
        this(new BigDecimal(Utils.getFiniteDoubleOrDefault(amount, 0d)), unit);
    }

    /**
     * Creates an amount with {@link java.math.BigDecimal} value.
     * @see ECommerceAmount#ECommerceAmount(long, String)
     * @see ECommerceAmount#ECommerceAmount(double, String)
     * *
     * @param amount Amount value as {@link java.math.BigDecimal}.
     * @param unit Amount unit. For example, "USD", "RUB", etc.
     */
    public ECommerceAmount(@NonNull BigDecimal amount, @NonNull String unit) {
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * @see ECommerceAmount#ECommerceAmount(double, String)
     * @see ECommerceAmount#ECommerceAmount(long, String)
     * @see ECommerceAmount#ECommerceAmount(java.math.BigDecimal, String)
     *
     * @return decimal amount
     */
    @NonNull
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @see ECommerceAmount#ECommerceAmount(double, String)
     * @see ECommerceAmount#ECommerceAmount(long, String)
     * @see ECommerceAmount#ECommerceAmount(java.math.BigDecimal, String)
     *
     * @return unit
     */
    @NonNull
    public String getUnit() {
        return unit;
    }

    @Override
    @NonNull
    public String toString() {
        return "ECommerceAmount{" +
                "amount=" + amount +
                ", unit='" + unit + '\'' +
                '}';
    }
}

