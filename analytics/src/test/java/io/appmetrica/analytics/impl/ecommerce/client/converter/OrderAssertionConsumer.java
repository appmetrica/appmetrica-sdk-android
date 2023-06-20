package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import java.util.function.Consumer;

class OrderAssertionConsumer implements Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Order>> {

    private String expectedOrderUuid = "";
    private String expectedOrderId = "";
    private Ecommerce.ECommerceEvent.Payload expectedPayload;
    private int expectedTotalItemsCount;
    private Ecommerce.ECommerceEvent.OrderCartItem[] expectedOrderCartItems;

    @Override
    public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Order> assertions) {
        try {
            assertions.withFinalFieldOnly(false);
            assertions.checkField("uuid", expectedOrderUuid.getBytes());
            assertions.checkField("orderId", expectedOrderId.getBytes());
            assertions.checkField("payload", expectedPayload);
            assertions.checkField("totalItemsCount", expectedTotalItemsCount);
            assertions.checkField("items", expectedOrderCartItems);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OrderAssertionConsumer setExpectedOrderUuid(String expectedOrderUuid) {
        this.expectedOrderUuid = expectedOrderUuid;
        return this;
    }

    public OrderAssertionConsumer setExpectedOrderId(String expectedOrderId) {
        this.expectedOrderId = expectedOrderId;
        return this;
    }

    public OrderAssertionConsumer setExpectedPayload(Ecommerce.ECommerceEvent.Payload expectedPayload) {
        this.expectedPayload = expectedPayload;
        return this;
    }

    public OrderAssertionConsumer setExpectedOrderCartItems(
            Ecommerce.ECommerceEvent.OrderCartItem[] expectedOrderCartItems
    ) {
        return setExpectedOrderCartItems(
                expectedOrderCartItems,
                expectedOrderCartItems == null ? 0 : expectedOrderCartItems.length
        );
    }

    public OrderAssertionConsumer setExpectedOrderCartItems(
            Ecommerce.ECommerceEvent.OrderCartItem[] expectedOrderCartItems,
            int expectedTotalItemsCount
    ) {
        this.expectedOrderCartItems = expectedOrderCartItems;
        this.expectedTotalItemsCount = expectedTotalItemsCount;
        return this;
    }

    public OrderAssertionConsumer setExpectedOrderCartItems(Ecommerce.ECommerceEvent.CartItem[] expectedCartItems) {
        return setExpectedOrderCartItems(expectedCartItems, 0);
    }

    public OrderAssertionConsumer setExpectedOrderCartItems(Ecommerce.ECommerceEvent.CartItem[] expectedCartItems,
                                                            int numberInCartOffset) {

        Ecommerce.ECommerceEvent.OrderCartItem[] orderCartItems = expectedCartItems == null ? null :
                new Ecommerce.ECommerceEvent.OrderCartItem[expectedCartItems.length];

        if (orderCartItems != null) {
            for (int i = 0; i < orderCartItems.length; i++) {
                orderCartItems[i] = new Ecommerce.ECommerceEvent.OrderCartItem();
                orderCartItems[i].numberInCart = numberInCartOffset + i;
                orderCartItems[i].item = expectedCartItems[i];
            }
        }

        return setExpectedOrderCartItems(orderCartItems);
    }
}
