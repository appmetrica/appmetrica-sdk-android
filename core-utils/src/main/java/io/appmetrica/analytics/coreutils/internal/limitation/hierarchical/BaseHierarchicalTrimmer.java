package io.appmetrica.analytics.coreutils.internal.limitation.hierarchical;

import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider;

public abstract class BaseHierarchicalTrimmer<V, M extends BytesTruncatedProvider>
        implements HierarchicalTrimmer<V, M> {

    private final int limit;

    public BaseHierarchicalTrimmer(int limit) {
        this.limit = limit;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public int getLimit() {
        return limit;
    }
}
