package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Collections;
import java.util.List;

public class ShownProductCardInfoEventConverter implements ECommerceEventConverter<ShownProductCardInfoEvent> {

    private static final String TAG = "[ShownProductCardInfoConverter]";

    @NonNull
    private final ScreenConverter screenConverter;
    @NonNull
    private final ProductConverter productConverter;

    public ShownProductCardInfoEventConverter() {
        this(new ScreenConverter(), new ProductConverter());
    }

    @NonNull
    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> fromModel(
            @NonNull ShownProductCardInfoEvent value
    ) {
        Ecommerce.ECommerceEvent proto = new Ecommerce.ECommerceEvent();
        proto.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_CARD;
        proto.shownProductCardInfo = new Ecommerce.ECommerceEvent.ShownProductCardInfo();

        Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> screenResult =
                screenConverter.fromModel(value.screen);
        proto.shownProductCardInfo.screen = screenResult.result;

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> productResult =
                productConverter.fromModel(value.product);
        proto.shownProductCardInfo.product = productResult.result;

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(screenResult, productResult);

        if (YLogger.DEBUG && totalTruncationInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated (screen + product) = %d (%d + %d)",
                    totalTruncationInfo.getBytesTruncated(), screenResult.getBytesTruncated(),
                    productResult.getBytesTruncated()
            );
        }

        return Collections.singletonList(
                new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(proto, totalTruncationInfo)
        );
    }

    @NonNull
    @Override
    public ShownProductCardInfoEvent toModel(
            @NonNull List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> nano
    ) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ShownProductCardInfoEventConverter(@NonNull ScreenConverter screenConverter,
                                       @NonNull ProductConverter productConverter) {
        this.screenConverter = screenConverter;
        this.productConverter = productConverter;
    }
}
