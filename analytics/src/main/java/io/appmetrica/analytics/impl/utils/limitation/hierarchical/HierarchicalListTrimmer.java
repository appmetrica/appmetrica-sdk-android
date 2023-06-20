package io.appmetrica.analytics.impl.utils.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import java.util.ArrayList;
import java.util.List;

public class HierarchicalListTrimmer<T> extends BaseHierarchicalTrimmer<List<T>, CollectionTrimInfo> {

    private static final String TAG = "[InternalListTrimmer]";

    @Nullable
    private final HierarchicalTrimmer<T, BytesTruncatedProvider> itemTrimmer;

    public HierarchicalListTrimmer(int limit) {
        this(limit, null);
    }

    public HierarchicalListTrimmer(int limit,
                                   @Nullable HierarchicalTrimmer<T, BytesTruncatedProvider> itemTrimmer) {
        super(limit);
        this.itemTrimmer = itemTrimmer;
    }

    @NonNull
    @Override
    public TrimmingResult<List<T>, CollectionTrimInfo> trim(@Nullable List<T> input) {
        List<T> resultList = input;
        int itemsDropped = 0;
        int bytesTruncated = 0;
        if (input != null && (input.size() > getLimit() || itemTrimmer != null)) {
            resultList = new ArrayList<T>();
            int count = 0;
            for (T item : input) {
                if (count < getLimit()) {
                    T itemToAdd = item;
                    if (itemTrimmer != null) {
                        TrimmingResult<T, BytesTruncatedProvider> trimmingResult = itemTrimmer.trim(item);
                        itemToAdd = trimmingResult.value;
                        bytesTruncated += trimmingResult.getBytesTruncated();
                        if (YLogger.DEBUG && !Utils.areEqual(item, trimmingResult.value)) {
                            YLogger.debug(
                                    ECommerceConstants.FEATURE_TAG + TAG,
                                    "List item #%d. Trim list item: \"%s\" -> \"%s\" with bytesTruncated = %d. " +
                                            "Current subtotal bytesTruncated = %d.",
                                    count, item, trimmingResult.value, trimmingResult.metaInfo, bytesTruncated
                            );
                        }
                    }
                    resultList.add(itemToAdd);
                } else {
                    int itemSize = byteSizeOf(item);
                    itemsDropped++;
                    bytesTruncated += itemSize;
                    YLogger.debug(
                            ECommerceConstants.FEATURE_TAG + TAG,
                            "List item #%d. Dropped item #%d with value = \"%s\". Add bytesTruncated = %d. " +
                                    "Subtotal bytesTruncated = %d",
                            count, itemsDropped-1, item, itemSize, bytesTruncated
                    );
                }
                count++;
            }
        }
        return new TrimmingResult<List<T>, CollectionTrimInfo>(
                resultList,
                new CollectionTrimInfo(itemsDropped, bytesTruncated)
        );
    }

    protected int byteSizeOf(@Nullable T entity) {
        return 0;
    }

    @VisibleForTesting
    @Nullable
    public HierarchicalTrimmer<T, BytesTruncatedProvider> getItemTrimmer() {
        return itemTrimmer;
    }
}
