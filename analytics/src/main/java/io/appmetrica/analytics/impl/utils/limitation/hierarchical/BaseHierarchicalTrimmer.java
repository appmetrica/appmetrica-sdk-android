package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;

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
