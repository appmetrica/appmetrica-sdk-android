package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider;
import java.util.List;

public interface ProtoSerializable {

    List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto();

}
