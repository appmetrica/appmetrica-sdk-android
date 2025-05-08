package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ProtoMessageSizeCalculatorCoverageTest extends CommonTest {

    private final ProtoMessageSizeCalculator calculator = new ProtoMessageSizeCalculator();

    @Test
    public void coverage() throws Exception {
        try (MockedStatic<CodedOutputByteBufferNano> sCodedOutputByteBufferNano = Mockito.mockStatic(CodedOutputByteBufferNano.class)) {

            Ecommerce.ECommerceEvent event = new Ecommerce.ECommerceEvent();
            event.shownScreenInfo = new Ecommerce.ECommerceEvent.ShownScreenInfo();
            event.cartActionInfo = new Ecommerce.ECommerceEvent.CartActionInfo();
            event.orderInfo = new Ecommerce.ECommerceEvent.OrderInfo();
            event.shownProductCardInfo = new Ecommerce.ECommerceEvent.ShownProductCardInfo();
            event.shownProductDetailsInfo = new Ecommerce.ECommerceEvent.ShownProductDetailsInfo();
            event.type = Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_BEGIN_CHECKOUT;

            ProtoObjectPropertyAssertions<Ecommerce.ECommerceEvent> assertions =
                new ProtoObjectPropertyAssertions<Ecommerce.ECommerceEvent>(event);
            int fieldsNumber = assertions.getUnverifiedFieldsCount();

            assertions
                .checkField("type", Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_BEGIN_CHECKOUT)
                .checkFieldNonNull("shownScreenInfo")
                .checkFieldNonNull("cartActionInfo")
                .checkFieldNonNull("orderInfo")
                .checkFieldNonNull("shownProductCardInfo")
                .checkFieldNonNull("shownProductDetailsInfo")
                .checkAll();

            when(CodedOutputByteBufferNano.computeInt32Size(1, event.type)).thenReturn(1);
            when(CodedOutputByteBufferNano.computeMessageSize(2, event.shownScreenInfo)).thenReturn(10);
            when(CodedOutputByteBufferNano.computeMessageSize(3, event.shownProductCardInfo)).thenReturn(100);
            when(CodedOutputByteBufferNano.computeMessageSize(4, event.shownProductDetailsInfo)).thenReturn(1000);
            when(CodedOutputByteBufferNano.computeMessageSize(5, event.cartActionInfo)).thenReturn(10000);
            when(CodedOutputByteBufferNano.computeMessageSize(6, event.orderInfo)).thenReturn(100000);

            int size = calculator.computeAdditionalNestedSize(event);
            assertThat(size).isEqualTo(111111);
            assertThat(size).as("all fields are considered").isEqualTo(ones(fieldsNumber));
        }
    }

    private int ones(int num) {
        int result = 0;
        int addendum = 1;
        for (int i = 0; i < num; i++) {
            result += addendum;
            addendum *= 10;
        }
        return result;
    }
}
