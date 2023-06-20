package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public class HierarchicalStringListTrimmer extends HierarchicalListTrimmer<String> {

    public HierarchicalStringListTrimmer(int limit, int itemLengthLimit) {
        this(limit, new HierarchicalStringTrimmer(itemLengthLimit));
    }

    @Override
    protected int byteSizeOf(@Nullable String entity) {
        return StringUtils.getUTF8Bytes(entity).length;
    }

    @VisibleForTesting
    public HierarchicalStringListTrimmer(int limit, @NonNull HierarchicalStringTrimmer itemTrimmer) {
        super(limit, itemTrimmer);
    }
}
