package io.appmetrica.analytics.impl.ecommerce.client.trimmer;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalListTrimmer;

public class PriceHierarchicalComponentsTrimmer extends HierarchicalListTrimmer<AmountWrapper> {

    private static final int BIG_DECIMAL_APPROX_BYTES_SIZE = 12;

    public PriceHierarchicalComponentsTrimmer(int limit) {
        super(limit);
    }

    @Override
    protected int byteSizeOf(@Nullable AmountWrapper entity) {
        return entity == null ? 0 : BIG_DECIMAL_APPROX_BYTES_SIZE + StringUtils.getUtf8BytesLength(entity.unit);
    }
}
