package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes a product.
 */
public class ECommerceProduct {

    @NonNull
    private final String sku;
    @Nullable
    private String name;
    @Nullable
    private List<String> categoriesPath;
    @Nullable
    private Map<String, String> payload;
    @Nullable
    private ECommercePrice actualPrice;
    @Nullable
    private ECommercePrice originalPrice;
    @Nullable
    private List<String> promocodes;

    /**
     * Creates a product.
     *
     * @param sku product SKU (Stock Keeping Unit).
     */
    public ECommerceProduct(@NonNull String sku) {
        this.sku = sku;
    }

    /**
     * Returns the product SKU.
     *
     * @return sku
     * @see ECommerceProduct#ECommerceProduct(String)
     */
    @NonNull
    public String getSku() {
        return sku;
    }

    /**
     * Returns the product name.
     *
     * @return name
     * @see ECommerceProduct#setName(String)
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets product name.
     *
     * @param name Name of the product.
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the product categories path.
     *
     * @return categories path
     * @see ECommerceProduct#setCategoriesPath(java.util.List)
     */
    @Nullable
    public List<String> getCategoriesPath() {
        return categoriesPath;
    }

    /**
     * Sets product categories path.
     *
     * @param categoriesPath Categories-wise path to the product.
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setCategoriesPath(@Nullable List<String> categoriesPath) {
        this.categoriesPath = categoriesPath;
        return this;
    }

    /**
     * Returns the product payload.
     *
     * @return payload
     * @see ECommerceProduct#setPayload(java.util.Map)
     */
    @Nullable
    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload Payload - additional key-value structured data with various content.
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setPayload(@Nullable Map<String, String> payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Returns the actual price of the product.
     *
     * @return actual price
     * @see ECommerceProduct#setActualPrice(ECommercePrice)
     */
    @Nullable
    public ECommercePrice getActualPrice() {
        return actualPrice;
    }

    /**
     * Sets actual price of the product - price after all discounts and promocodes are applied.
     *
     * @param actualPrice Actual price of the product.
     *                    @see ECommercePrice
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setActualPrice(@Nullable ECommercePrice actualPrice) {
        this.actualPrice = actualPrice;
        return this;
    }

    /**
     * Returns the original price of the product.
     *
     * @return original price
     * @see ECommerceProduct#setOriginalPrice(ECommercePrice)
     */
    @Nullable
    public ECommercePrice getOriginalPrice() {
        return originalPrice;
    }

    /**
     * Sets original price of the product.
     *
     * @param originalPrice Original price of the product.
     *                      @see ECommercePrice
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setOriginalPrice(@Nullable ECommercePrice originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    /**
     * Returns the product promocodes.
     *
     * @return promocodes
     * @see ECommerceProduct#setPromocodes(java.util.List)
     */
    @Nullable
    public List<String> getPromocodes() {
        return promocodes;
    }

    /**
     * Sets promocodes.
     *
     * @param promocodes List of promocodes applied to the product.
     * @return same {@link ECommerceProduct} object
     */
    @NonNull
    public ECommerceProduct setPromocodes(@Nullable List<String> promocodes) {
        this.promocodes = promocodes;
        return this;
    }

    @Override
    public String toString() {
        return "ECommerceProduct{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", categoriesPath=" + categoriesPath +
                ", payload=" + payload +
                ", actualPrice=" + actualPrice +
                ", originalPrice=" + originalPrice +
                ", promocodes=" + promocodes +
                '}';
    }
}
