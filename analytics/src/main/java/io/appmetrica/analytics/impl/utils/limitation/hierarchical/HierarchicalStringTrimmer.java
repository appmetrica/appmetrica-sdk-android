package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;

public class HierarchicalStringTrimmer extends BaseHierarchicalTrimmer<String, BytesTruncatedProvider> {

    private static final String TAG = "[InternalStringTrimmer]";

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
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Trim \"%s\" -> \"%s\" with bytes truncated = %d",
                    input, truncatedString, truncatedBytes
            );
        }
        return new TrimmingResult<String, BytesTruncatedProvider>(
                truncatedString,
                new BytesTruncatedInfo(truncatedBytes)
        );
    }
}
