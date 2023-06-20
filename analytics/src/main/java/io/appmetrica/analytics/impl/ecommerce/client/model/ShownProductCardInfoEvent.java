package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownProductCardInfoEventConverter;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public class ShownProductCardInfoEvent extends ECommerceEvent {

    @NonNull
    public final ProductWrapper product;
    @NonNull
    public final ScreenWrapper screen;
    @NonNull
    private final ECommerceEventConverter<ShownProductCardInfoEvent> converter;

    public ShownProductCardInfoEvent(@NonNull ECommerceProduct product,
                                     @NonNull ECommerceScreen screen) {
        this(new ProductWrapper(product), new ScreenWrapper(screen), new ShownProductCardInfoEventConverter());
    }

    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
        return converter.fromModel(this);
    }

    @Override
    public String toString() {
        return "ShownProductCardInfoEvent{" +
                "product=" + product +
                ", screen=" + screen +
                ", converter=" + converter +
                '}';
    }

    @NonNull
    @Override
    public String getPublicDescription() {
        return "shown product card info";
    }

    @VisibleForTesting
    public ShownProductCardInfoEvent(@NonNull ProductWrapper product,
                                     @NonNull ScreenWrapper screen,
                                     @NonNull ECommerceEventConverter<ShownProductCardInfoEvent> converter) {
        this.product = product;
        this.screen = screen;
        this.converter = converter;
    }

    @VisibleForTesting
    @NonNull
    public ECommerceEventConverter<ShownProductCardInfoEvent> getConverter() {
        return converter;
    }
}
