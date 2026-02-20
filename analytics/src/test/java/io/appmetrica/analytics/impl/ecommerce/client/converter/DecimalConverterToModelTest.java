package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DecimalConverterToModelTest extends CommonTest {

    @Mock
    private Ecommerce.ECommerceEvent.Decimal input;

    private DecimalConverter decimalConverter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        decimalConverter = new DecimalConverter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        decimalConverter.toModel(input);
    }
}
