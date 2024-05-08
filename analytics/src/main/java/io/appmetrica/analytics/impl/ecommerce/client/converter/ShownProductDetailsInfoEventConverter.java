package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Collections;
import java.util.List;

public class ShownProductDetailsInfoEventConverter implements ECommerceEventConverter<ShownProductDetailInfoEvent> {

    private static final String TAG = "[ShownProductDetailsInfoConverter]";

    @NonNull
    private final ProductConverter productConverter;
    @NonNull
    private final ReferrerConverter referrerConverter;

    public ShownProductDetailsInfoEventConverter() {
        this(new ProductConverter(), new ReferrerConverter());
    }

    @NonNull
    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> fromModel(
            @NonNull ShownProductDetailInfoEvent value
    ) {
        Ecommerce.ECommerceEvent proto = new Ecommerce.ECommerceEvent();
        proto.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_DETAILS;
        proto.shownProductDetailsInfo = new Ecommerce.ECommerceEvent.ShownProductDetailsInfo();

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> productResult =
                productConverter.fromModel(value.product);
        proto.shownProductDetailsInfo.product = productResult.result;

        Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> referrerResult = null;
        if (value.referrer != null) {
             referrerResult = referrerConverter.fromModel(value.referrer);
            proto.shownProductDetailsInfo.referrer = referrerResult.result;
        }

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(productResult, referrerResult);

        if (totalTruncationInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated (product + referrer) = %d (%d + %d)",
                    totalTruncationInfo.getBytesTruncated(), productResult.getBytesTruncated(),
                    referrerResult == null ? 0 : referrerResult.getBytesTruncated()
            );
        }

        return Collections.singletonList(
                new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(proto, totalTruncationInfo)
        );
    }

    @NonNull
    @Override
    public ShownProductDetailInfoEvent toModel(
            @NonNull List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> nano
    ) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ShownProductDetailsInfoEventConverter(@NonNull ProductConverter productConverter,
                                          @NonNull ReferrerConverter referrerConverter) {
        this.productConverter = productConverter;
        this.referrerConverter = referrerConverter;
    }
}
