package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.ecommerce.client.converter.CartActionInfoEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public class CartActionInfoEvent extends ECommerceEvent {

    public final int eventType;
    @NonNull
    public final CartItemWrapper cartItem;
    @NonNull
    private final ECommerceEventConverter<CartActionInfoEvent> converter;

    public static final int EVENT_TYPE_ADD_TO_CART = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_ADD_TO_CART;
    public static final int EVENT_TYPE_REMOVE_FROM_CART =
            Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_REMOVE_FROM_CART;

    public CartActionInfoEvent(int eventType, @NonNull ECommerceCartItem cartItem) {
        this(eventType, new CartItemWrapper(cartItem), new CartActionInfoEventConverter());
    }

    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
        return converter.fromModel(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "CartActionInfoEvent{" +
                "eventType=" + eventType +
                ", cartItem=" + cartItem +
                ", converter=" + converter +
                '}';
    }

    @NonNull
    @Override
    public String getPublicDescription() {
        switch (eventType) {
            case EVENT_TYPE_ADD_TO_CART:
                return "add cart item info";
            case EVENT_TYPE_REMOVE_FROM_CART:
                return "remove cart item info";
            default:
                return "unknown cart action info";
        }
    }

    @VisibleForTesting
    public CartActionInfoEvent(int eventType,
                               @NonNull CartItemWrapper cartItem,
                               @NonNull ECommerceEventConverter<CartActionInfoEvent> converter) {
        this.eventType = eventType;
        this.cartItem = cartItem;
        this.converter = converter;
    }

    @VisibleForTesting
    @NonNull
    public ECommerceEventConverter<CartActionInfoEvent> getConverter() {
        return converter;
    }
}
