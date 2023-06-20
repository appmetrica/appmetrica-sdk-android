package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownProductDetailsInfoEventConverter;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public class ShownProductDetailInfoEvent extends ECommerceEvent {

    @NonNull
    public final ProductWrapper product;
    @Nullable
    public final ReferrerWrapper referrer;
    @NonNull
    private final ECommerceEventConverter<ShownProductDetailInfoEvent> converter;

    public ShownProductDetailInfoEvent(@NonNull ECommerceProduct product,
                                       @Nullable ECommerceReferrer referrer) {
        this(
                new ProductWrapper(product),
                referrer == null ? null : new ReferrerWrapper(referrer),
                new ShownProductDetailsInfoEventConverter()
        );
    }

    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
        return converter.fromModel(this);
    }

    @Override
    public String toString() {
        return "ShownProductDetailInfoEvent{" +
                "product=" + product +
                ", referrer=" + referrer +
                ", converter=" + converter +
                '}';
    }

    @NonNull
    @Override
    public String getPublicDescription() {
        return "shown product details info";
    }

    @VisibleForTesting
    public ShownProductDetailInfoEvent(@NonNull ProductWrapper product,
                                       @Nullable ReferrerWrapper referrer,
                                       @NonNull ECommerceEventConverter<ShownProductDetailInfoEvent> converter) {
        this.product = product;
        this.referrer = referrer;
        this.converter = converter;
    }

    @VisibleForTesting
    @NonNull
    public ECommerceEventConverter<ShownProductDetailInfoEvent> getConverter() {
        return converter;
    }
}
