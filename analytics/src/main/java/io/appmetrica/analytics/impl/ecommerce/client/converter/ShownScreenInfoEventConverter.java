package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ecommerce.ECommerceConstants;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Collections;
import java.util.List;

public class ShownScreenInfoEventConverter implements ECommerceEventConverter<ShownScreenInfoEvent> {

    private static final String TAG = "[ShownScreenInfoConverter]";

    @NonNull
    private final ScreenConverter screenConverter;

    public ShownScreenInfoEventConverter() {
        this(new ScreenConverter());
    }

    @NonNull
    @Override
    public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> fromModel(
            @NonNull ShownScreenInfoEvent value) {
        Ecommerce.ECommerceEvent proto = new Ecommerce.ECommerceEvent();
        proto.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_SCREEN;
        proto.shownScreenInfo = new Ecommerce.ECommerceEvent.ShownScreenInfo();

        Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider> screenConvertingResult =
                screenConverter.fromModel(value.screen);
        proto.shownScreenInfo.screen = screenConvertingResult.result;

        BytesTruncatedProvider totalTruncationInfo = BytesTruncatedInfo.total(screenConvertingResult);

        if (screenConvertingResult.getBytesTruncated() > 0) {
            DebugLogger.INSTANCE.info(
                    ECommerceConstants.FEATURE_TAG + TAG,
                    "Bytes truncated (screen) = %d (%d)",
                    screenConvertingResult.getBytesTruncated(), screenConvertingResult.getBytesTruncated()
            );
        }

        return Collections.singletonList(
                new Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>(proto, totalTruncationInfo)
        );
    }

    @NonNull
    @Override
    public ShownScreenInfoEvent toModel(@NonNull List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> nano) {
       throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    ShownScreenInfoEventConverter(@NonNull ScreenConverter screenConverter) {
        this.screenConverter = screenConverter;
    }
}
