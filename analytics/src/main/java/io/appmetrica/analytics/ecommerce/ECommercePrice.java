package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/**
 * Describes price of a product.
 */
public class ECommercePrice {

    @NonNull
    private final ECommerceAmount fiat;
    @Nullable
    private List<ECommerceAmount> internalComponents;

    /**
     * Creates a price.
     *
     * @param fiat Amount in fiat money.
     *             @see ECommerceAmount
     */
    public ECommercePrice(@NonNull ECommerceAmount fiat) {
        this.fiat = fiat;
    }

    /**
     * @see ECommercePrice#ECommercePrice(ECommerceAmount)
     *
     * @return fiat amount
     */
    @NonNull
    public ECommerceAmount getFiat() {
        return fiat;
    }

    /**
     * @see ECommercePrice#setInternalComponents(java.util.List)
     *
     * @return internal components
     */
    @Nullable
    public List<ECommerceAmount> getInternalComponents() {
        return internalComponents;
    }

    /**
     * Sets price internal components - amounts in internal currency.
     *
     * @param internalComponents List of amounts.
     *                           @see ECommerceAmount
     * @return same {@link ECommercePrice} object
     */
    public ECommercePrice setInternalComponents(@Nullable List<ECommerceAmount> internalComponents) {
        this.internalComponents = internalComponents;
        return this;
    }

    @Override
    public String toString() {
        return "ECommercePrice{" +
                "fiat=" + fiat +
                ", internalComponents=" + internalComponents +
                '}';
    }
}
