package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ecommerce.ECommerceEventProvider;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProtoSerializable;

/**
 * ECommerce event object.
 *
 * Use static methods of this class to form e-commerce event.
 * There are several different types of e-commerce events for different user actions.
 * Each method corresponds to one specific type. See method descriptions for more info.
 */
public abstract class ECommerceEvent implements ProtoSerializable {

    @NonNull
    private static ECommerceEventProvider provider = new ECommerceEventProvider();

    /**
     * Creates e-commerce ShowScreenEvent.
     * Use this event to report user opening some page: product list, search screen, main page, etc.
     *
     * @param screen Screen that has been opened.
     *               @see ECommerceScreen
     * @return e-commerce ShowScreenEvent
     */
    @NonNull
    public static ECommerceEvent showScreenEvent(@NonNull ECommerceScreen screen) {
        return provider.showScreenEvent(screen);
    }

    /**
     * Creates e-commerce ShowProductCardEvent.
     * Use this event to report user viewing product card among others in a list.
     * Best practise is to consider product card viewed if it has been shown on screen for more than N seconds.
     *
     * @param product Product that has been viewed.
     *                @see ECommerceProduct
     * @param screen Screen where the product is shown.
     *               @see ECommerceScreen
     * @return e-commerce ShowProductCardEvent
     */
    @NonNull
    public static ECommerceEvent showProductCardEvent(@NonNull ECommerceProduct product,
                                                      @NonNull ECommerceScreen screen) {
        return provider.showProductCardEvent(product, screen);
    }

    /**
     * Creates e-commerce ShowProductDetailsEvent.
     * Use this method to report user viewing product card by opening its own page.
     *
     * @param product Product that has been viewed.
     *                @see ECommerceProduct
     * @param referrer Info about the source of transition to shown product card.
     *                 @see ECommerceReferrer
     * @return e-commerce ShowProductDetailsEvent
     */
    @NonNull
    public static ECommerceEvent showProductDetailsEvent(@NonNull ECommerceProduct product,
                                                         @Nullable ECommerceReferrer referrer) {
        return provider.showProductDetailsEvent(product, referrer);
    }

    /**
     * Creates e-commerce AddCartItemEvent.
     * Use this method to report user adding an item to cart.
     *
     * @param cartItem Item that has been added to cart.
     *                 @see ECommerceCartItem
     * @return e-commerce AddCartItemEvent
     */
    @NonNull
    public static ECommerceEvent addCartItemEvent(@NonNull ECommerceCartItem cartItem) {
        return provider.addCartItemEvent(cartItem);
    }

    /**
     * Creates e-commerce RemoveCartItemEvent.
     * Use this method to report user removing an item form cart.
     *
     * @param cartItem Item that has been removed from cart.
     *                 @see ECommerceCartItem
     * @return e-commerce RemoveCartItemEvent
     */
    @NonNull
    public static ECommerceEvent removeCartItemEvent(@NonNull ECommerceCartItem cartItem) {
        return provider.removeCartItemEvent(cartItem);
    }

    /**
     * Creates e-commerce BeginCheckoutEvent.
     * Use this event to report user begin checkout a purchase.
     *
     * @param order Various info about purchase.
     *              @see ECommerceOrder
     * @return e-commerce BeginCheckoutEvent
     */
    @NonNull
    public static ECommerceEvent beginCheckoutEvent(@NonNull ECommerceOrder order) {
        return provider.beginCheckoutEvent(order);
    }

    /**
     * Creates e-commerce PurchaseEvent.
     * Use this event to report user complete a purchase.
     *
     * @param order Various info about purchase.
     *              @see ECommerceOrder
     * @return e-commerce PurchaseEvent
     */
    @NonNull
    public static ECommerceEvent purchaseEvent(@NonNull ECommerceOrder order) {
        return provider.purchaseEvent(order);
    }

    @VisibleForTesting
    static void setProvider(@NonNull ECommerceEventProvider provider) {
        ECommerceEvent.provider = provider;
    }

    @NonNull
    static ECommerceEventProvider getProvider() {
        return provider;
    }

    @NonNull
    public String getPublicDescription() {
        return "E-commerce base event";
    }
}
