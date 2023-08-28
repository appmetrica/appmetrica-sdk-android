package io.appmetrica.analytics.impl.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent;

public class ECommerceEventProvider {

    @NonNull
    public ECommerceEvent showScreenEvent(@NonNull ECommerceScreen screen) {
        return new ShownScreenInfoEvent(screen);
    }

    @NonNull
    public ECommerceEvent showProductCardEvent(@NonNull ECommerceProduct product, @NonNull ECommerceScreen screen) {
        return new ShownProductCardInfoEvent(product, screen);
    }

    @NonNull
    public ECommerceEvent showProductDetailsEvent(@NonNull ECommerceProduct product,
                                                  @Nullable ECommerceReferrer referrer) {
        return new ShownProductDetailInfoEvent(product, referrer);
    }

    @NonNull
    public ECommerceEvent addCartItemEvent(@NonNull ECommerceCartItem cartItem) {
        return new CartActionInfoEvent(CartActionInfoEvent.EVENT_TYPE_ADD_TO_CART, cartItem);
    }

    @NonNull
    public ECommerceEvent removeCartItemEvent(@NonNull ECommerceCartItem cartItem) {
        return new CartActionInfoEvent(CartActionInfoEvent.EVENT_TYPE_REMOVE_FROM_CART, cartItem);
    }

    @NonNull
    public ECommerceEvent beginCheckoutEvent(@NonNull ECommerceOrder order) {
        return new OrderInfoEvent(OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT, order);
    }

    @NonNull
    public ECommerceEvent purchaseEvent(@NonNull ECommerceOrder order) {
        return new OrderInfoEvent(OrderInfoEvent.EVENT_TYPE_PURCHASE, order);
    }
}
