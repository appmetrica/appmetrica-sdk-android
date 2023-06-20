package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import java.util.List;
import java.util.Map;

public class ProductWrapper {

    @NonNull
    public final String sku;
    @Nullable
    public final String name;
    @Nullable
    public final List<String> categoriesPath;
    @Nullable
    public final Map<String, String> payload;
    @Nullable
    public final PriceWrapper actualPrice;
    @Nullable
    public final PriceWrapper originalPrice;
    @Nullable
    public final List<String> promocodes;

    public ProductWrapper(@NonNull ECommerceProduct input) {
        this(
                input.getSku(),
                input.getName(),
                CollectionUtils.arrayListCopyOfNullableCollection(input.getCategoriesPath()),
                CollectionUtils.mapCopyOfNullableMap(input.getPayload()),
                input.getActualPrice() == null ? null : new PriceWrapper(input.getActualPrice()),
                input.getOriginalPrice() == null ? null : new PriceWrapper(input.getOriginalPrice()),
                CollectionUtils.arrayListCopyOfNullableCollection(input.getPromocodes())
        );
    }

    @Override
    public String toString() {
        return "ProductWrapper{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", categoriesPath=" + categoriesPath +
                ", payload=" + payload +
                ", actualPrice=" + actualPrice +
                ", originalPrice=" + originalPrice +
                ", promocodes=" + promocodes +
                '}';
    }

    @VisibleForTesting
    public ProductWrapper(@NonNull String sku,
                          @Nullable String name,
                          @Nullable List<String> categoriesPath,
                          @Nullable Map<String, String> payload,
                          @Nullable PriceWrapper actualPrice,
                          @Nullable PriceWrapper originalPrice,
                          @Nullable List<String> promocodes) {
        this.sku = sku;
        this.name = name;
        this.categoriesPath = categoriesPath;
        this.payload = payload;
        this.actualPrice = actualPrice;
        this.originalPrice = originalPrice;
        this.promocodes = promocodes;
    }
}
