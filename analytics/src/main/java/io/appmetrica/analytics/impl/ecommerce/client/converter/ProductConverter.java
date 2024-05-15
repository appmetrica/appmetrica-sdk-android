package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class ProductConverter
        implements Converter<ProductWrapper, Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>> {

    private static final String TAG = "[ProductConverter]";

    @NonNull
    private final PayloadConverter payloadConverter;
    @NonNull
    private final PriceConverter priceConverter;
    @NonNull
    private final CategoryConverter categoryConverter;
    @NonNull
    private final PromocodesConverter promocodesConverter;
    @NonNull
    private final HierarchicalStringTrimmer skuTrimmer;
    @NonNull
    private final HierarchicalStringTrimmer nameTrimmer;

    public ProductConverter() {
        this(
                new PayloadConverter(),
                new PriceConverter(),
                new CategoryConverter(),
                new PromocodesConverter(),
                new HierarchicalStringTrimmer(Limits.PRODUCT_SKU_LENGTH),
                new HierarchicalStringTrimmer(Limits.PRODUCT_NAME_LENGTH)
        );
    }

    @SuppressWarnings("checkstyle:methodlength")
    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> fromModel(@NonNull ProductWrapper value) {
        Ecommerce.ECommerceEvent.Product proto = new Ecommerce.ECommerceEvent.Product();

        TrimmingResult<String, BytesTruncatedProvider> skuTrimmingResult = skuTrimmer.trim(value.sku);
        proto.sku = StringUtils.getUTF8Bytes(skuTrimmingResult.value);

        TrimmingResult<String, BytesTruncatedProvider> nameTrimmingResult = nameTrimmer.trim(value.name);
        proto.name = StringUtils.getUTF8Bytes(nameTrimmingResult.value);

        Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider> categoriesProtoResult = null;
        if (value.categoriesPath != null) {
            categoriesProtoResult = categoryConverter.fromModel(value.categoriesPath);
            proto.category = categoriesProtoResult.result;
        }

        Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> payloadResult = null;
        if (value.payload != null) {
            payloadResult = payloadConverter.fromModel(value.payload);
            proto.payload = payloadResult.result;
        }

        Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> actualPriceResult = null;
        if (value.actualPrice != null) {
            actualPriceResult = priceConverter.fromModel(value.actualPrice);
            proto.actualPrice = actualPriceResult.result;
        }

        Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> originalPriceResult = null;
        if (value.originalPrice != null) {
            originalPriceResult = priceConverter.fromModel(value.originalPrice);
            proto.originalPrice = originalPriceResult.result;
        }

        Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider> promocodesResult = null;
        if (value.promocodes != null) {
            promocodesResult = promocodesConverter.fromModel(value.promocodes);
            proto.promoCodes = promocodesResult.result;
        }

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(
                skuTrimmingResult, nameTrimmingResult, categoriesProtoResult, payloadResult, actualPriceResult,
                originalPriceResult, promocodesResult);

        if (totalTruncationInfo.getBytesTruncated() > 0) {
            DebugLogger.info(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated (sku (%d) + name (%d) + categoriesPath (%d) + payload (%d) + " +
                            "originalPrice (%d) + actualPrice (%d) + promocodes (%d)) = %d",
                    skuTrimmingResult.metaInfo, nameTrimmingResult.metaInfo,
                    categoriesProtoResult == null ? 0 : categoriesProtoResult.getBytesTruncated(),
                    payloadResult == null ? 0 : payloadResult.getBytesTruncated(),
                    originalPriceResult == null ? 0 : originalPriceResult.getBytesTruncated(),
                    actualPriceResult == null ? 0 : actualPriceResult.getBytesTruncated(),
                    promocodesResult == null ? 0 : promocodesResult.getBytesTruncated(),
                    totalTruncationInfo.getBytesTruncated()
            );
        }
        return new Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>(proto, totalTruncationInfo);
    }

    @NonNull
    @Override
    public ProductWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ProductConverter(@NonNull PayloadConverter payloadConverter,
                     @NonNull PriceConverter priceConverter,
                     @NonNull CategoryConverter categoryConverter,
                     @NonNull PromocodesConverter promocodesConverter,
                     @NonNull HierarchicalStringTrimmer skuTrimmer,
                     @NonNull HierarchicalStringTrimmer nameTrimmer) {
        this.payloadConverter = payloadConverter;
        this.priceConverter = priceConverter;
        this.categoryConverter = categoryConverter;
        this.promocodesConverter = promocodesConverter;
        this.skuTrimmer = skuTrimmer;
        this.nameTrimmer = nameTrimmer;
    }
}
