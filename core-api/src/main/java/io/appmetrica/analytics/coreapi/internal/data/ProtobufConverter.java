package io.appmetrica.analytics.coreapi.internal.data;

import io.appmetrica.analytics.protobuf.nano.MessageNano;

public interface ProtobufConverter<S, P extends MessageNano> extends Converter<S, P> {
}
