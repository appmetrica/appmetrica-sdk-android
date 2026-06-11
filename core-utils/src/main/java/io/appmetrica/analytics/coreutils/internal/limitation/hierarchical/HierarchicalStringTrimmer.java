package io.appmetrica.analytics.coreutils.internal.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class HierarchicalStringTrimmer extends BaseHierarchicalTrimmer<String, BytesTruncatedProvider> {

    private static final String TAG = "[HierarchicalStringTrimmer]";

    public HierarchicalStringTrimmer(int limit) {
        super(limit);
    }

    @Override
    @NonNull
    public TrimmingResult<String, BytesTruncatedProvider> trim(@Nullable String input) {
        String truncatedString = input;
        int truncatedBytes = 0;
        if (truncatedString != null && truncatedString.length() > getLimit()) {
            truncatedString = truncatedString.substring(0, getLimit());
            truncatedBytes = input.getBytes().length - truncatedString.getBytes().length;
            DebugLogger.INSTANCE.info(
                TAG,
                "Trim \"%s\" -> \"%s\" with bytes truncated = %d",
                input,
                truncatedString,
                truncatedBytes
            );
        }
        return new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedString,
                new BytesTruncatedInfo(truncatedBytes)
        );
    }
}
