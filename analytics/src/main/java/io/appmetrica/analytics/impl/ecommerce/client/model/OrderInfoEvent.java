package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.OrderInfoEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public class OrderInfoEvent extends ECommerceEvent {

    public final int eventType;
    @NonNull
    public final OrderWrapper order;
    @NonNull
    private final ECommerceEventConverter<OrderInfoEvent> converter;

    public static final int EVENT_TYPE_BEGIN_CHECKOUT = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_BEGIN_CHECKOUT;
    public static final int EVENT_TYPE_PURCHASE = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_PURCHASE;

    public OrderInfoEvent(int eventType, @NonNull ECommerceOrder order) {
        this(eventType, new OrderWrapper(order), new OrderInfoEventConverter());
    }

    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
        return converter.fromModel(this);
    }

    @Override
    public String toString() {
        return "OrderInfoEvent{" +
                "eventType=" + eventType +
                ", order=" + order +
                ", converter=" + converter +
                '}';
    }

    @NonNull
    @Override
    public String getPublicDescription() {
        return "order info";
    }

    @VisibleForTesting
    public OrderInfoEvent(int eventType,
                          @NonNull OrderWrapper order,
                          @NonNull ECommerceEventConverter<OrderInfoEvent> converter) {
        this.eventType = eventType;
        this.order = order;
        this.converter = converter;
    }

    @VisibleForTesting
    @NonNull
    public ECommerceEventConverter<OrderInfoEvent> getConverter() {
        return converter;
    }
}
