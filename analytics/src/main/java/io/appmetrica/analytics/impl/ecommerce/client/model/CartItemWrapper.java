package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import java.math.BigDecimal;

public class CartItemWrapper {

    @NonNull
    public final ProductWrapper product;
    @NonNull
    public final BigDecimal quantity;
    @NonNull
    public final PriceWrapper revenue;
    @Nullable
    public final ReferrerWrapper referrer;

    public CartItemWrapper(@NonNull ECommerceCartItem input) {
        this(
                new ProductWrapper(input.getProduct()),
                input.getQuantity(),
                new PriceWrapper(input.getRevenue()),
                input.getReferrer() == null ? null : new ReferrerWrapper(input.getReferrer())
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "CartItemWrapper{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", revenue=" + revenue +
                ", referrer=" + referrer +
                '}';
    }

    @VisibleForTesting
    public CartItemWrapper(@NonNull ProductWrapper product,
                           @NonNull BigDecimal quantity,
                           @NonNull PriceWrapper revenue,
                           @Nullable ReferrerWrapper referrer) {
        this.product = product;
        this.quantity = quantity;
        this.revenue = revenue;
        this.referrer = referrer;
    }
}
