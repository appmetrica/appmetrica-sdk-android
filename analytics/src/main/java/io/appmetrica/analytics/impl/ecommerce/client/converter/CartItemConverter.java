package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class CartItemConverter
        implements Converter<CartItemWrapper, Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>> {

    private static final String TAG = "[CartItemConverter]";

    @NonNull
    private final ProductConverter productConverter;
    @NonNull
    private final DecimalConverter decimalConverter;
    @NonNull
    private final PriceConverter priceConverter;
    @NonNull
    private final ReferrerConverter referrerConverter;

    public CartItemConverter() {
        this(
                new ProductConverter(),
                new DecimalConverter(),
                new PriceConverter(),
                new ReferrerConverter()
        );
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider> fromModel(@NonNull CartItemWrapper value) {
        Ecommerce.ECommerceEvent.CartItem proto = new Ecommerce.ECommerceEvent.CartItem();

        Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> productResult =
                productConverter.fromModel(value.product);
        proto.product = productResult.result;

        proto.quantity = decimalConverter.fromModel(value.quantity);

        Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> revenueResult =
                priceConverter.fromModel(value.revenue);
        proto.revenue = revenueResult.result;

        Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> referrerResult = null;
        if (value.referrer != null) {
            referrerResult = referrerConverter.fromModel(value.referrer);
            proto.referrer = referrerResult.result;
        }

        BytesTruncatedProvider totalTruncationInfo =
                BytesTruncatedInfo.total(productResult, revenueResult, referrerResult);

        if (totalTruncationInfo.getBytesTruncated() > 0) {
            DebugLogger.info(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated (product + revenue + referrer ) = %d(%d + %d + %d)",
                    totalTruncationInfo.getBytesTruncated(), productResult.metaInfo, revenueResult.metaInfo,
                    referrerResult == null ? 0 : referrerResult.getBytesTruncated()
            );
        }

        return new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(proto, totalTruncationInfo);
    }

    @NonNull
    @Override
    public CartItemWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    CartItemConverter(@NonNull ProductConverter productConverter,
                      @NonNull DecimalConverter decimalConverter,
                      @NonNull PriceConverter priceConverter,
                      @NonNull ReferrerConverter referrerConverter) {
        this.productConverter = productConverter;
        this.decimalConverter = decimalConverter;
        this.priceConverter = priceConverter;
        this.referrerConverter = referrerConverter;
    }
}
