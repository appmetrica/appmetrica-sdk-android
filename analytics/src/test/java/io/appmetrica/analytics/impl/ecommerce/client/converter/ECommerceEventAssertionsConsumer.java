package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import java.util.function.Consumer;

public class ECommerceEventAssertionsConsumer implements Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent>> {

    private final int expectedEventType;
    private Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.OrderInfo>> orderInfoAssertionConsumer;
    private Ecommerce.ECommerceEvent.CartActionInfo expectedCartActionProto;
    private Ecommerce.ECommerceEvent.ShownScreenInfo expectedShowScreenInfo;
    private Ecommerce.ECommerceEvent.ShownProductDetailsInfo expectedShowProductDetailsInfo;
    private Ecommerce.ECommerceEvent.ShownProductCardInfo expectedShowProductCardInfo;

    public ECommerceEventAssertionsConsumer(int expectedEventType) {
        this.expectedEventType = expectedEventType;
    }

    @Override
    public void accept(ObjectPropertyAssertions<Ecommerce.ECommerceEvent> assertions) {
        try {
            assertions.withFinalFieldOnly(false)
                .checkField("type", expectedEventType)
                .checkFieldRecursively("orderInfo", orderInfoAssertionConsumer)
                .checkFieldComparingFieldByField("cartActionInfo", expectedCartActionProto)
                .checkFieldComparingFieldByField("shownScreenInfo", expectedShowScreenInfo)
                .checkFieldComparingFieldByField("shownProductDetailsInfo", expectedShowProductDetailsInfo)
                .checkFieldComparingFieldByField("shownProductCardInfo", expectedShowProductCardInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ECommerceEventAssertionsConsumer setOrderInfoAssertionConsumer(
        Consumer<ObjectPropertyAssertions<Ecommerce.ECommerceEvent.OrderInfo>> orderInfoAssertionConsumer
    ) {
        this.orderInfoAssertionConsumer = orderInfoAssertionConsumer;
        return this;
    }

    public ECommerceEventAssertionsConsumer setExpectedCartActionProto(
        Ecommerce.ECommerceEvent.CartActionInfo expectedCartActionProto) {
        this.expectedCartActionProto = expectedCartActionProto;
        return this;
    }

    public ECommerceEventAssertionsConsumer setExpectedShowScreenInfo(
        Ecommerce.ECommerceEvent.ShownScreenInfo expectedShowScreenInfo) {
        this.expectedShowScreenInfo = expectedShowScreenInfo;
        return this;
    }

    public ECommerceEventAssertionsConsumer setExpectedShowProductDetailsInfo(
        Ecommerce.ECommerceEvent.ShownProductDetailsInfo expectedShowProductDetailsInfo) {
        this.expectedShowProductDetailsInfo = expectedShowProductDetailsInfo;
        return this;
    }

    public ECommerceEventAssertionsConsumer setExpectedShowProductCardInfo(
        Ecommerce.ECommerceEvent.ShownProductCardInfo expectedShowProductCardInfo) {
        this.expectedShowProductCardInfo = expectedShowProductCardInfo;
        return this;
    }
}
