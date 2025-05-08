package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import java.util.function.Consumer;

class OrderInfoAssertionsConsumer implements Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.OrderInfo>> {

    private final OrderAssertionConsumer orderAssertionConsumer;

    public OrderInfoAssertionsConsumer(OrderAssertionConsumer orderAssertionConsumer) {
        this.orderAssertionConsumer = orderAssertionConsumer;
    }

    @Override
    public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent.OrderInfo> assertions) {
        try {
            assertions.withFinalFieldOnly(false);
            assertions.checkFieldRecursively("order", orderAssertionConsumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
