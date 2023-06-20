package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;

public interface ECommerceEventConverter<T extends ECommerceEvent>
        extends Converter<T, List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>> {
}
