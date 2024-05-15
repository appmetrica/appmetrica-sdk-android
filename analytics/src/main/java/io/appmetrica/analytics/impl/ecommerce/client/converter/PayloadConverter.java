package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.Map;

public class PayloadConverter implements
        Converter<Map<String, String>, Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>> {

    private static final String TAG = "[PayloadConverter]";

    @NonNull
    private final HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer payloadTrimmer;

    public PayloadConverter() {
        this(new HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
                Limits.TOTAL_PAYLOAD_BYTES,
                Limits.PAYLOAD_KEY_LENGTH,
                Limits.PAYLOAD_VALUE_LENGTH
        ));
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> fromModel(
            @NonNull Map<String, String> value) {
        TrimmingResult<Map<String, String>, CollectionTrimInfo> truncatedPayload =
                payloadTrimmer.trim(value);

        Ecommerce.ECommerceEvent.Payload proto = new Ecommerce.ECommerceEvent.Payload();
        proto.truncatedPairsCount = truncatedPayload.metaInfo.itemsDropped;
        Map<String, String> payload = truncatedPayload.value;
        if (payload != null) {
            proto.pairs = new Ecommerce.ECommerceEvent.Payload.Pair[payload.size()];
            int i = 0;
            for (Map.Entry<String, String> pair : payload.entrySet()) {
                proto.pairs[i] = new Ecommerce.ECommerceEvent.Payload.Pair();
                proto.pairs[i].key = StringUtils.getUTF8Bytes(pair.getKey());
                proto.pairs[i].value = StringUtils.getUTF8Bytes(pair.getValue());

                i++;
            }
        }

        if (truncatedPayload.metaInfo.bytesTruncated > 0) {
            DebugLogger.info(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Truncate payload %s -> %s with dropped %d pairs and bytesTruncated = %d",
                    value, truncatedPayload.value, truncatedPayload.metaInfo.itemsDropped,
                    truncatedPayload.metaInfo.bytesTruncated
            );
        }

        return new Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider>(proto, truncatedPayload.metaInfo);
    }

    @NonNull
    @Override
    public Map<String, String> toModel(@NonNull Result<Ecommerce.ECommerceEvent.Payload, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    public PayloadConverter(@NonNull HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer payloadTrimmer) {
        this.payloadTrimmer = payloadTrimmer;
    }
}
