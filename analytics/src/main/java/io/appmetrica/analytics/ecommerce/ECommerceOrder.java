package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes an order - info about a cart purchase.
 */
public class ECommerceOrder {

    @NonNull
    private final String identifier;
    @NonNull
    private final List<ECommerceCartItem> cartItems;
    @Nullable
    private Map<String, String> payload;

    /**
     * Creates an order.
     *
     * @param identifier Order identifier.
     * @param cartItems List of items in the cart.
     *                  @see ECommerceCartItem
     */
    public ECommerceOrder(@NonNull String identifier,
                          @NonNull List<ECommerceCartItem> cartItems) {
        this.identifier = identifier;
        this.cartItems = cartItems;
    }

    /**
     * Sets payload.
     *
     * @param payload Payload - additional key-value structured data with various content.
     * @return same {@link ECommerceOrder} object
     */
    public ECommerceOrder setPayload(@Nullable Map<String, String> payload) {
        this.payload = payload;
        return this;
    }

    /**
     * @see ECommerceOrder#setPayload(java.util.Map)
     *
     * @return payload
     */
    @Nullable
    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     * @see ECommerceOrder#ECommerceOrder(String, java.util.List)
     *
     * @return order identifier
     */
    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @see ECommerceOrder#ECommerceOrder(String, java.util.List)
     *
     * @return items in the cart
     */
    @NonNull
    public List<ECommerceCartItem> getCartItems() {
        return cartItems;
    }

    @Override
    public String toString() {
        return "ECommerceOrder{" +
                "identifier='" + identifier + '\'' +
                ", cartItems=" + cartItems +
                ", payload=" + payload +
                '}';
    }

}
