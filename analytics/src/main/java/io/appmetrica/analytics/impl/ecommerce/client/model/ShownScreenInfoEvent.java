package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownScreenInfoEventConverter;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public class ShownScreenInfoEvent extends ECommerceEvent {

    @NonNull
    public final ScreenWrapper screen;
    @NonNull
    private final ECommerceEventConverter<ShownScreenInfoEvent> converter;

    public ShownScreenInfoEvent(@NonNull ECommerceScreen screen) {
        this(new ScreenWrapper(screen), new ShownScreenInfoEventConverter());
    }

    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
        return converter.fromModel(this);
    }

    @Override
    public String toString() {
        return "ShownScreenInfoEvent{" +
                "screen=" + screen +
                ", converter=" + converter +
                '}';
    }

    @NonNull
    @Override
    public String getPublicDescription() {
        return "shown screen info";
    }

    @VisibleForTesting
    public ShownScreenInfoEvent(@NonNull ScreenWrapper screen,
                                @NonNull ECommerceEventConverter<ShownScreenInfoEvent> converter) {
        this.screen = screen;
        this.converter = converter;
    }

    @VisibleForTesting
    @NonNull
    public ECommerceEventConverter<ShownScreenInfoEvent> getConverter() {
        return converter;
    }
}
