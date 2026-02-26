package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.Utils;
import java.math.BigDecimal;

/**
 * Describes an item in a cart.
 */
public class ECommerceCartItem {

    @NonNull
    private final ECommerceProduct product;
    @NonNull
    private final BigDecimal quantity;
    @NonNull
    private final ECommercePrice revenue;
    @Nullable
    private ECommerceReferrer referrer;

    /**
     * Creates CartItem.
     *
     * @param product Item product.
     *                @see ECommerceProduct
     * @param revenue Total price of the cart item. Considers quantity, applied discounts, etc.
     *                @see ECommercePrice
     * @param quantityMicros Quantity of item product in micros (actual quantity multiplied by 10^6).
     *                       @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, double)
     *                       @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, java.math.BigDecimal)
     */
    public ECommerceCartItem(@NonNull ECommerceProduct product,
                             @NonNull ECommercePrice revenue,
                             long quantityMicros) {
        this(product, revenue, Utils.microsToBigDecimal(quantityMicros));
    }

    /**
     * Creates CartItem.
     *
     * @param product Item product.
     *                @see ECommerceProduct
     * @param revenue Total price of the cart item. Considers quantity, applied discounts, etc.
     *                @see ECommercePrice
     * @param quantity Quantity of item product as double.
     *                 {@link java.lang.Double#POSITIVE_INFINITY}, {@link java.lang.Double#NEGATIVE_INFINITY}
     *                 and {@link java.lang.Double#NaN} will be treated as 0.
     *                 @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, long)
     *                 @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, java.math.BigDecimal)
     */
    public ECommerceCartItem(@NonNull ECommerceProduct product,
                             @NonNull ECommercePrice revenue,
                             double quantity) {
        this(product, revenue, new BigDecimal(Utils.getFiniteDoubleOrDefault(quantity, 0d)));
    }

    /**
     * Creates CartItem.
     *
     * @param product Item product.
     *                @see ECommerceProduct
     * @param revenue Total price of the cart item. Considers quantity, applied discounts, etc.
     *                @see ECommercePrice
     * @param quantity Quantity of item product as {@link java.math.BigDecimal}.
     *                 @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, long)
     *                 @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, double)
     */
    public ECommerceCartItem(@NonNull ECommerceProduct product,
                             @NonNull ECommercePrice revenue,
                             @NonNull BigDecimal quantity) {
        this.product = product;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    /**
     * Returns the item product.
     *
     * @return item product
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, java.math.BigDecimal)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, double)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, long)
     */
    @NonNull
    public ECommerceProduct getProduct() {
        return product;
    }

    /**
     * Returns the item product quantity.
     *
     * @return item product quantity
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, java.math.BigDecimal)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, double)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, long)
     */
    @NonNull
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns the item revenue.
     *
     * @return item revenue
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, java.math.BigDecimal)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, double)
     * @see ECommerceCartItem#ECommerceCartItem(ECommerceProduct, ECommercePrice, long)
     */
    @NonNull
    public ECommercePrice getRevenue() {
        return revenue;
    }

    /**
     * Sets cart item referrer which describes a way item was added to cart.
     *
     * @param referrer Referrer.
     *                 @see ECommerceReferrer
     * @return same {@link ECommerceCartItem} object
     */
    @NonNull
    public ECommerceCartItem setReferrer(@Nullable ECommerceReferrer referrer) {
        this.referrer = referrer;
        return this;
    }

    /**
     * Returns the cart item referrer.
     *
     * @return referrer
     * @see ECommerceCartItem#setReferrer(ECommerceReferrer)
     */
    @Nullable
    public ECommerceReferrer getReferrer() {
        return referrer;
    }

    @Override
    public String toString() {
        return "ECommerceCartItem{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", revenue=" + revenue +
                ", referrer=" + referrer +
                '}';
    }
}
