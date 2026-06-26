package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;
import java.math.BigDecimal;

/**
 * Monetary or in-game amount for a product flow offer.
 */
public class OfferPrice {

    @NonNull
    private final BigDecimal amount;
    @NonNull
    private final String unit;

    /**
     * Creates an offer price from an exact decimal value.
     *
     * @param amount Exact monetary or in-game amount.
     * @param unit   Currency code or in-game unit name; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     */
    public OfferPrice(@NonNull BigDecimal amount, @NonNull String unit) {
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Creates an offer price from a micros value (1/1 000 000 of the base unit).
     *
     * @param amountMicros Amount in micros.
     * @param unit         Currency code or in-game unit name; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     */
    public OfferPrice(long amountMicros, @NonNull String unit) {
        this(WrapUtils.microsToBigDecimal(amountMicros), unit);
    }

    /**
     * Creates an offer price from a double value. Non-finite values are treated as {@code 0}.
     *
     * @param amount Exact amount as a double.
     * @param unit   Currency code or in-game unit name; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     */
    public OfferPrice(double amount, @NonNull String unit) {
        this(BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefault(amount, 0d)), unit);
    }

    /**
     * Returns the monetary or in-game amount.
     *
     * @return amount.
     */
    @NonNull
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the currency code or in-game unit name.
     * Truncated to
     * {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     * characters if longer.
     *
     * @return unit.
     */
    @NonNull
    public String getUnit() {
        return unit;
    }

    @NonNull
    @Override
    public String toString() {
        return "OfferPrice{" +
            "amount=" + amount +
            ", unit='" + unit + '\'' +
            '}';
    }
}
