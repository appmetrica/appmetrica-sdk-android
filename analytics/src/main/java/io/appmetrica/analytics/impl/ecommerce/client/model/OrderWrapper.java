package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderWrapper {

    @NonNull
    public final String uuid;
    @NonNull
    public final String identifier;
    @NonNull
    public final List<CartItemWrapper> cartItems;
    @Nullable
    public final Map<String, String> payload;

    public OrderWrapper(@NonNull ECommerceOrder input) {
        this(
                UUID.randomUUID().toString(),
                input.getIdentifier(),
                OrderWrapper.toCartItems(input.getCartItems()),
                CollectionUtils.mapCopyOfNullableMap(input.getPayload())
        );
    }

    @NonNull
    private static List<CartItemWrapper> toCartItems(@NonNull List<ECommerceCartItem> input) {
        List<CartItemWrapper> result = new ArrayList<CartItemWrapper>(input.size());
        for (ECommerceCartItem inputItem : input) {
            result.add(new CartItemWrapper(inputItem));
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "OrderWrapper{" +
                "uuid='" + uuid + '\'' +
                ", identifier='" + identifier + '\'' +
                ", cartItems=" + cartItems +
                ", payload=" + payload +
                '}';
    }

    @VisibleForTesting
    public OrderWrapper(@NonNull String uuid,
                        @NonNull String identifier,
                        @NonNull List<CartItemWrapper> cartItems,
                        @Nullable Map<String, String> payload) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.cartItems = cartItems;
        this.payload = payload;
    }
}
