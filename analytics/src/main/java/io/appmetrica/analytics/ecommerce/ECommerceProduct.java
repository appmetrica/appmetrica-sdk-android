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
     * @see ECommerceProduct#ECommerceProduct(String)
     *
     * @return sku
     */
    @NonNull
    public String getSku() {
        return sku;
    }

    /**
     * @see ECommerceProduct#setName(String)
     *
     * @return name
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
     * @see ECommerceProduct#setCategoriesPath(java.util.List)
     *
     * @return categories path
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
     * @see ECommerceProduct#setPayload(java.util.Map)
     *
     * @return payload
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
     * @see ECommerceProduct#setActualPrice(ECommercePrice)
     *
     * @return actual price
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
     * @see ECommerceProduct#setOriginalPrice(ECommercePrice)
     *
     * @return original price
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
     * @see ECommerceProduct#setPromocodes(java.util.List)
     *
     * @return promocodes
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
