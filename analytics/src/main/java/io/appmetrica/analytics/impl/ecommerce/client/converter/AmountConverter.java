package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;

public class AmountConverter
        implements Converter<AmountWrapper, Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>> {

    private static final String TAG = "[AmountConverter]";

    @NonNull
    private final DecimalConverter decimalConverter;
    @NonNull
    private final HierarchicalStringTrimmer currencyTrimmer;

    public AmountConverter() {
        this(new DecimalConverter(), new HierarchicalStringTrimmer(Limits.CURRENCY_LENGTH));
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> fromModel(@NonNull AmountWrapper value) {
        Ecommerce.ECommerceEvent.Amount proto = new Ecommerce.ECommerceEvent.Amount();
        proto.value = decimalConverter.fromModel(value.amount);

        TrimmingResult<String, BytesTruncatedProvider> unitTypeTruncationTrimmingResult =
                currencyTrimmer.trim(value.unit);
        proto.unitType = StringUtils.getUTF8Bytes(unitTypeTruncationTrimmingResult.value);

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(unitTypeTruncationTrimmingResult);
        if (YLogger.DEBUG && totalTruncationInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Truncate amount. Total bytes truncated = %d",
                    totalTruncationInfo.getBytesTruncated()
            );
        }
        return new Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider>(proto, totalTruncationInfo);
    }

    @NonNull
    @Override
    public AmountWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    AmountConverter(@NonNull DecimalConverter decimalConverter,
                    @NonNull HierarchicalStringTrimmer currencyTrimmer) {
        this.decimalConverter = decimalConverter;
        this.currencyTrimmer = currencyTrimmer;
    }
}
