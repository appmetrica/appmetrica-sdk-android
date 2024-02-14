package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.trimmer.PriceHierarchicalComponentsTrimmer;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.List;

public class PriceConverter
        implements Converter<PriceWrapper, Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>> {

    private static final String TAG = "[PriceConverter]";

    @NonNull
    private final AmountConverter amountConverter;
    @NonNull
    private final PriceHierarchicalComponentsTrimmer priceInternalComponentsTrimmer;

    public PriceConverter() {
        this(new AmountConverter(), new PriceHierarchicalComponentsTrimmer(Limits.PRICE_INTERNAL_COMPONENTS_COUNT));
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> fromModel(@NonNull PriceWrapper value) {
        Ecommerce.ECommerceEvent.Price proto = new Ecommerce.ECommerceEvent.Price();

        Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> fiatResult =
                amountConverter.fromModel(value.fiat);
        proto.fiat = fiatResult.result;

        TrimmingResult<List<AmountWrapper>, CollectionTrimInfo> componentsTrimmingResult =
                priceInternalComponentsTrimmer.trim(value.internalComponents);

        int internalComponentsTotalAmountBytesTruncated = 0;
        if (!Utils.isNullOrEmpty(componentsTrimmingResult.value)) {
            proto.internalComponents = new Ecommerce.ECommerceEvent.Amount[componentsTrimmingResult.value.size()];
            Result<Ecommerce.ECommerceEvent.Amount, BytesTruncatedProvider> itemResult = null;
            for (int i = 0; i < componentsTrimmingResult.value.size(); i++) {
                itemResult = amountConverter.fromModel(componentsTrimmingResult.value.get(i));
                proto.internalComponents[i] = itemResult.result;
                internalComponentsTotalAmountBytesTruncated += itemResult.getBytesTruncated();
            }
        }
        BytesTruncatedProvider internalComponentAmountsTruncationInfo =
                new BytesTruncatedInfo(internalComponentsTotalAmountBytesTruncated);

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(fiatResult, componentsTrimmingResult,
                internalComponentAmountsTruncationInfo);

        if (YLogger.DEBUG && totalTruncationInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "TotalBytesTruncated (fiat + droppedInternalComponents + truncatedInternalComponents) = " +
                            "%d (%d + %d + %d)",
                    totalTruncationInfo.getBytesTruncated(),
                    fiatResult.getBytesTruncated(),
                    componentsTrimmingResult.getBytesTruncated(),
                    internalComponentAmountsTruncationInfo.getBytesTruncated()
            );
        }

        return new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(proto, totalTruncationInfo);
    }

    @NonNull
    @Override
    public PriceWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    PriceConverter(@NonNull AmountConverter amountConverter,
                   @NonNull PriceHierarchicalComponentsTrimmer priceInternalComponentsTrimmer) {
        this.amountConverter = amountConverter;
        this.priceInternalComponentsTrimmer = priceInternalComponentsTrimmer;
    }

}
