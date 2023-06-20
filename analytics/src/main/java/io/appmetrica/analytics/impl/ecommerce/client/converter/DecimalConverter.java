package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.DecimalProtoModel;
import java.math.BigDecimal;

public class DecimalConverter implements ProtobufConverter<BigDecimal, Ecommerce.ECommerceEvent.Decimal> {

    @NonNull
    @Override
    public Ecommerce.ECommerceEvent.Decimal fromModel(@NonNull BigDecimal value) {
        DecimalProtoModel model = DecimalProtoModel.fromDecimal(value);

        Ecommerce.ECommerceEvent.Decimal proto = new Ecommerce.ECommerceEvent.Decimal();
        proto.mantissa = model.getMantissa();
        proto.exponent = model.getExponent();

        return proto;
    }

    @NonNull
    @Override
    public BigDecimal toModel(@NonNull Ecommerce.ECommerceEvent.Decimal nano) {
        throw new UnsupportedOperationException();
    }
}
