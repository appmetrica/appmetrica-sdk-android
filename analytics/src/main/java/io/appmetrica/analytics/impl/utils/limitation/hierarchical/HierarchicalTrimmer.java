package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;

public interface HierarchicalTrimmer<V, M extends BytesTruncatedProvider> {

    @NonNull
    TrimmingResult<V, M> trim(@Nullable V input);
}
