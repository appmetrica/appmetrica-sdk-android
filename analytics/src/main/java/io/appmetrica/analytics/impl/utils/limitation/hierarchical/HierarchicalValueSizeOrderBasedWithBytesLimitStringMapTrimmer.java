package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
        extends BaseHierarchicalTrimmer<Map<String, String>, CollectionTrimInfo> {

    private static final String TAG = "[InternalValueSizeOrderBasedWithBytesLimitStringMapTrimmer]";

    @NonNull
    private final HierarchicalStringTrimmer keyTrimmer;
    @NonNull
    private final HierarchicalStringTrimmer valueTrimmer;

    private Comparator<Map.Entry<String, String>> mapEntryByValueSizeComparator =
            new Comparator<Map.Entry<String, String>>() {
        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            String firstValue = o1.getValue();
            String secondValue = o2.getValue();
            int firstLength = StringUtils.getUtf8BytesLength(firstValue);
            int secondLength = StringUtils.getUtf8BytesLength(secondValue);
            return (firstLength < secondLength) ? -1 : ((firstLength == secondLength) ? 0 : 1);
        }
    };

    public HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(int bytesLimit,
                                                                         int keyLengthLimit,
                                                                         int valueLengthLimit) {
        this(
                bytesLimit,
                new HierarchicalStringTrimmer(keyLengthLimit),
                new HierarchicalStringTrimmer(valueLengthLimit)
        );
    }

    @NonNull
    @Override
    public TrimmingResult<Map<String, String>, CollectionTrimInfo> trim(@Nullable Map<String, String> input) {
        Map<String, String> truncatedMap = null;
        int bytesTruncated = 0;
        int pairsDropped = 0;

        if (input != null) {
            truncatedMap = new HashMap<String, String>();

            int currentSize = 0;
            boolean maxCapacityReached = false;
            for (Map.Entry<String, String> entry : sortedEntries(input)) {
                TrimmingResult<String, BytesTruncatedProvider> keyTrimmingResult = keyTrimmer.trim(entry.getKey());
                TrimmingResult<String, BytesTruncatedProvider> valueTrimmingResult =
                        valueTrimmer.trim(entry.getValue());
                int entrySizeBeforeTruncation = sizeOf(entry.getKey()) + sizeOf(entry.getValue());
                int trimmedEntrySize = sizeOf(keyTrimmingResult.value) + sizeOf(valueTrimmingResult.value);
                if (maxCapacityReached || (currentSize + trimmedEntrySize > getLimit())) {
                    YLogger.debug(
                            ECommerceConstants.FEATURE_TAG + TAG,
                            "Map limit reached so drop map entry. Already dropped %d pairs. " +
                                    "Max capacity reached = %b; actual map size = %d; limit = %d. " +
                                    "Drop pair \"{%s -> %s}\". Entry size = %d",
                            pairsDropped, maxCapacityReached, currentSize, getLimit(), entry.getKey(),
                            entry.getValue(), entrySizeBeforeTruncation
                    );
                    maxCapacityReached = true;
                    pairsDropped ++;
                    bytesTruncated += entrySizeBeforeTruncation;
                } else {
                    bytesTruncated += keyTrimmingResult.getBytesTruncated();
                    bytesTruncated += valueTrimmingResult.getBytesTruncated();
                    currentSize += sizeOf(keyTrimmingResult.value) + sizeOf(valueTrimmingResult.value);
                    truncatedMap.put(keyTrimmingResult.value, valueTrimmingResult.value);
                }
            }
            if (YLogger.DEBUG && (pairsDropped != 0 || bytesTruncated != 0)) {
                YLogger.debug(
                       ECommerceConstants.FEATURE_TAG + TAG,
                       "Trim map \"%s\" -> \"%s\": pairsDropped = %d; bytesTruncated = %d",
                        input, truncatedMap, pairsDropped, bytesTruncated
                );
            }
        }
        return new TrimmingResult<Map<String, String>, CollectionTrimInfo>(
                truncatedMap,
                new CollectionTrimInfo(pairsDropped, bytesTruncated)
        );
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<String, String>[] sortedEntries(@NonNull Map<String, String> input) {
        Set<Map.Entry<String, String>> entrySet = input.entrySet();
        Map.Entry<String, String>[] entries = entrySet.toArray(new Map.Entry[entrySet.size()]);
        Arrays.sort(entries, mapEntryByValueSizeComparator);
        return entries;
    }

    private int sizeOf(@Nullable String value) {
        return StringUtils.getUtf8BytesLength(value);
    }

    public HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
            int limit,
            @NonNull HierarchicalStringTrimmer keyTrimmer,
            @NonNull HierarchicalStringTrimmer valueTrimmer
    ) {
        super(limit);
        this.keyTrimmer = keyTrimmer;
        this.valueTrimmer = valueTrimmer;
    }
}
