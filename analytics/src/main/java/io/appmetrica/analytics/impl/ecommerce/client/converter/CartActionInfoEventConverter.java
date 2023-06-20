package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.Collections;
import java.util.List;

public class CartActionInfoEventConverter implements ECommerceEventConverter<CartActionInfoEvent> {

    private static final String TAG = "[CartActionInfoConverter]";

    @NonNull
    private final CartItemConverter cartItemConverter;

    public CartActionInfoEventConverter() {
        this(new CartItemConverter());
    }

    @NonNull
    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> fromModel(
            @NonNull CartActionInfoEvent value) {
        Ecommerce.ECommerceEvent proto = new Ecommerce.ECommerceEvent();
        proto.cartActionInfo = new Ecommerce.ECommerceEvent.CartActionInfo();

        Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider> cartItemConvertingResult =
                cartItemConverter.fromModel(value.cartItem);
        proto.cartActionInfo.item = cartItemConvertingResult.result;

        proto.type = value.eventType;

        BytesTruncatedProvider total = BytesTruncatedInfo.total(cartItemConvertingResult);

        if (YLogger.DEBUG && total.getBytesTruncated() > 0) {
            YLogger.d(ECommerceConstants.FEATURE_TAG + TAG, "Total bytes truncated = %d", total.getBytesTruncated());
        }

        return Collections.singletonList(new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(proto, total));
    }

    @NonNull
    @Override
    public CartActionInfoEvent toModel(@NonNull List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    CartActionInfoEventConverter(@NonNull CartItemConverter cartItemConverter) {
        this.cartItemConverter = cartItemConverter;
    }
}
