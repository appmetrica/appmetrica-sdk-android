package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.DecimalProtoModel;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class DecimalConverterTest extends CommonTest {

    private DecimalConverter decimalConverter;
    private BigDecimal inputDecimal;

    @Rule
    public final MockedStaticRule<DecimalProtoModel> staticProtoModelMock =
            new MockedStaticRule<>(DecimalProtoModel.class);
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private DecimalProtoModel mock;

    @Before
    public void setUp() throws Exception {
        staticProtoModelMock.getStaticMock().when(new MockedStatic.Verification() {
            @Override
            public void apply() {
                DecimalProtoModel.fromDecimal(any(BigDecimal.class));
            }
        }).thenReturn(mock);
        decimalConverter = new DecimalConverter();
        inputDecimal = new BigDecimal("250.213213");
    }

    @Test
    public void toProto() throws IllegalAccessException {
        int exponent = 45;
        long mantissa = 78;
        doReturn(exponent).when(mock).getExponent();
        doReturn(mantissa).when(mock).getMantissa();

        Ecommerce.ECommerceEvent.Decimal proto = decimalConverter.fromModel(inputDecimal);

        staticProtoModelMock.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() {
                DecimalProtoModel.fromDecimal(inputDecimal);
            }
        });

        ObjectPropertyAssertions<Ecommerce.ECommerceEvent.Decimal> adRevenueAssertions =
                new ProtoObjectPropertyAssertions<>(proto);

        adRevenueAssertions.checkField("mantissa", mantissa);
        adRevenueAssertions.checkField("exponent", exponent);

        adRevenueAssertions.checkAll();
    }

}
