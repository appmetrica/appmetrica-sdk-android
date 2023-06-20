package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;

public class ScreenConverter
        implements Converter<ScreenWrapper, Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>> {

    private static final String TAG = "[ScreenConverter]";

    @NonNull
    private final PayloadConverter payloadConverter;
    @NonNull
    private final CategoryConverter categoryConverter;
    @NonNull
    private final HierarchicalStringTrimmer nameTrimmer;
    @NonNull
    private final HierarchicalStringTrimmer searchQueryTrimmer;

    public ScreenConverter() {
        this(
                new PayloadConverter(),
                new CategoryConverter(),
                new HierarchicalStringTrimmer(Limits.SCREEN_NAME_LENGTH),
                new HierarchicalStringTrimmer(Limits.SCREEN_SEARCH_QUERY_LENGTH)
        );
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> fromModel(@NonNull ScreenWrapper value) {
        Ecommerce.ECommerceEvent.Screen proto = new Ecommerce.ECommerceEvent.Screen();

        TrimmingResult<String, BytesTruncatedProvider> nameTrimmingResult = nameTrimmer.trim(value.name);
        proto.name = StringUtils.getUTF8Bytes(nameTrimmingResult.value);

        Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider> categoryProtoResult = null;
        if (value.categoriesPath != null) {
            categoryProtoResult = categoryConverter.fromModel(value.categoriesPath);
            proto.category = categoryProtoResult.result;
        }

        TrimmingResult<String, BytesTruncatedProvider> searchQueryTrimmingResult =
                searchQueryTrimmer.trim(value.searchQuery);
        proto.searchQuery = StringUtils.getUTF8Bytes(searchQueryTrimmingResult.value);

        Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> payloadResult = null;
        if (value.payload != null) {
            payloadResult = payloadConverter.fromModel(value.payload);
            proto.payload = payloadResult.result;
        }

        BytesTruncatedProvider totalBytesTruncatedInfo = BytesTruncatedInfo.total(
                nameTrimmingResult,
                categoryProtoResult,
                searchQueryTrimmingResult,
                payloadResult
        );

        if (YLogger.DEBUG && totalBytesTruncatedInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated = name (%d) + categoriesPath (%d) + searchQuery (%d) + payload (%d) = %d",
                    nameTrimmingResult.getBytesTruncated(),
                    categoryProtoResult == null ? 0 : categoryProtoResult.getBytesTruncated(),
                    searchQueryTrimmingResult.getBytesTruncated(),
                    payloadResult == null ? 0 : payloadResult.getBytesTruncated(),
                    totalBytesTruncatedInfo.getBytesTruncated()
            );
        }

        return new Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>(proto, totalBytesTruncatedInfo);
    }

    @NonNull
    @Override
    public ScreenWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ScreenConverter(@NonNull PayloadConverter payloadConverter,
                    @NonNull CategoryConverter categoryConverter,
                    @NonNull HierarchicalStringTrimmer nameTrimmer,
                    @NonNull HierarchicalStringTrimmer searchQueryTrimmer) {
        this.payloadConverter = payloadConverter;
        this.categoryConverter = categoryConverter;
        this.nameTrimmer = nameTrimmer;
        this.searchQueryTrimmer = searchQueryTrimmer;
    }
}
