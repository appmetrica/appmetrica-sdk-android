package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringListTrimmer;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.List;

public class CategoryConverter
        implements Converter<List<String>, Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>> {

    private static final String TAG = "[CategoryConverter]";

    @NonNull
    private final HierarchicalStringListTrimmer categoryTrimmer;

    public CategoryConverter() {
        this(
                new HierarchicalStringListTrimmer(
                        Limits.CATEGORY_PATH_ITEMS_COUNT,
                        Limits.CATEGORY_PATH_ITEM_LENGTH
                )
        );
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider> fromModel(@NonNull List<String> value) {
        TrimmingResult<List<String>, CollectionTrimInfo> trimmedValue =
                categoryTrimmer.trim(value);

        Ecommerce.ECommerceEvent.Category category = new Ecommerce.ECommerceEvent.Category();
        category.path = StringUtils.getUTF8Bytes(trimmedValue.value);

        if (trimmedValue.metaInfo.bytesTruncated > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated = %d(%d items)",
                    trimmedValue.metaInfo.bytesTruncated,
                    trimmedValue.metaInfo.itemsDropped
            );
        }
        return new Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider>(category, trimmedValue.metaInfo);
    }

    @NonNull
    @Override
    public List<String> toModel(@NonNull Result<Ecommerce.ECommerceEvent.Category, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    public CategoryConverter(@NonNull HierarchicalStringListTrimmer categoryTrimmer) {
        this.categoryTrimmer = categoryTrimmer;
    }
}
