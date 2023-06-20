package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringListTrimmer;
import java.util.List;

public class PromocodesConverter
        implements Converter<List<String>, Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider>> {

    private static final String TAG = "[PromocodesConverter]";

    @NonNull
    private final HierarchicalStringListTrimmer promocodesTrimmer;

    public PromocodesConverter() {
        this(new HierarchicalStringListTrimmer(Limits.PROMOCODES_ITEMS_COUNT, Limits.PROMOCODES_ITEM_LENGTH));
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider> fromModel(@NonNull List<String> value) {
        TrimmingResult<List<String>, CollectionTrimInfo> promocodesTrimmingResult =
                promocodesTrimmer.trim(value);
        List<String> trimmedValue = promocodesTrimmingResult.value;

        Ecommerce.ECommerceEvent.PromoCode[] proto = new Ecommerce.ECommerceEvent.PromoCode[0];
        if (trimmedValue != null) {
             proto = new Ecommerce.ECommerceEvent.PromoCode[trimmedValue.size()];
            for (int i = 0; i < trimmedValue.size(); i++) {
                proto[i] = new Ecommerce.ECommerceEvent.PromoCode();
                proto[i].code = StringUtils.getUTF8Bytes(trimmedValue.get(i));
            }
        }

        if (YLogger.DEBUG && promocodesTrimmingResult.getBytesTruncated() > 0) {
            YLogger.debug(
                   ECommerceConstants.FEATURE_TAG + TAG,
                   "Trim %s -> %s with %d dropped items and bytesTruncated = %d",
                    value,
                    trimmedValue == null ? "null" : trimmedValue,
                    promocodesTrimmingResult.metaInfo.itemsDropped,
                    promocodesTrimmingResult.metaInfo.bytesTruncated
            );
        }

        return new Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider>(
                proto,
                promocodesTrimmingResult.metaInfo
        );
    }

    @NonNull
    @Override
    public List<String> toModel(@NonNull Result<Ecommerce.ECommerceEvent.PromoCode[], BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    public PromocodesConverter(@NonNull HierarchicalStringListTrimmer promocodesTrimmer) {
        this.promocodesTrimmer = promocodesTrimmer;
    }
}
