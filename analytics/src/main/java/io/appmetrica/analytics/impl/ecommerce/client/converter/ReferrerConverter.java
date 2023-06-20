package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.impl.utils.limitation.hierarchical.HierarchicalStringTrimmer;

public class ReferrerConverter
        implements Converter<ReferrerWrapper, Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider>> {

    private static final String TAG = "[ReferrerConverter]";

    @NonNull
    private final ScreenConverter screenConverter;
    @NonNull
    private final HierarchicalStringTrimmer typeTrimmer;
    @NonNull
    private final HierarchicalStringTrimmer idTrimmer;

    public ReferrerConverter() {
        this(
                new ScreenConverter(),
                new HierarchicalStringTrimmer(Limits.REFERRER_TYPE_LENGTH),
                new HierarchicalStringTrimmer(Limits.REFERRER_ID_LENGTH)
        );
    }

    @NonNull
    @Override
    public Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> fromModel(@NonNull ReferrerWrapper value) {
        Ecommerce.ECommerceEvent.Referrer proto = new Ecommerce.ECommerceEvent.Referrer();

        TrimmingResult<String, BytesTruncatedProvider> typeTrimmingResult = typeTrimmer.trim(value.type);
        proto.type = StringUtils.getUTF8Bytes(typeTrimmingResult.value);

        TrimmingResult<String, BytesTruncatedProvider> idTrimmingResult = idTrimmer.trim(value.identifier);
        proto.id = StringUtils.getUTF8Bytes(idTrimmingResult.value);

        Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> screenConvertingResult = null;
        if (value.screen != null) {
             screenConvertingResult = screenConverter.fromModel(value.screen);
            proto.screen = screenConvertingResult.result;
        }

        BytesTruncatedProvider totalTruncationInfo =
                BytesTruncatedInfo.total(typeTrimmingResult, idTrimmingResult, screenConvertingResult);

        if (YLogger.DEBUG && totalTruncationInfo.getBytesTruncated() > 0) {
            YLogger.debug(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Total bytes truncated (type + id + screen) = %d (%d + %d + %d)",
                    totalTruncationInfo.getBytesTruncated(), typeTrimmingResult.metaInfo, idTrimmingResult.metaInfo,
                    screenConvertingResult == null ? 0 : screenConvertingResult.getBytesTruncated()
            );
        }

        return new Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider>(proto, totalTruncationInfo);
    }

    @NonNull
    @Override
    public ReferrerWrapper toModel(@NonNull Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider> nano) {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ReferrerConverter(@NonNull ScreenConverter screenConverter,
                      @NonNull HierarchicalStringTrimmer typeTrimmer,
                      @NonNull HierarchicalStringTrimmer idTrimmer) {
        this.screenConverter = screenConverter;
        this.typeTrimmer = typeTrimmer;
        this.idTrimmer = idTrimmer;
    }
}
