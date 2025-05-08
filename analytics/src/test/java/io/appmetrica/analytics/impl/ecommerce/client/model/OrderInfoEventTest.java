package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import io.appmetrica.analytics.impl.ecommerce.ECommerceEventProviderTest;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.OrderInfoEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @see ECommerceEventProviderTest#orderEvent()
 */
@RunWith(RobolectricTestRunner.class)
public class OrderInfoEventTest extends CommonTest {

    @Mock
    private OrderWrapper orderWrapper;
    @Mock
    private ECommerceEventConverter<OrderInfoEvent> converter;
    @Mock
    private List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> proto;
    @Mock
    private ECommerceOrder eCommerceOrder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void converterFromDefaultConstructor() {
        assertThat(new OrderInfoEvent(0, eCommerceOrder).getConverter())
            .isInstanceOf(OrderInfoEventConverter.class);
    }

    @Test
    public void toProto() throws Exception {
        OrderInfoEvent orderInfoEvent = new OrderInfoEvent(0, orderWrapper, converter);
        when(converter.fromModel(orderInfoEvent)).thenReturn(proto);
        assertThat(orderInfoEvent.toProto()).isEqualTo(proto);
    }

    @Test
    public void getPublicDescription() {
        assertThat(new OrderInfoEvent(0, orderWrapper, converter).getPublicDescription()).isEqualTo("order info");
    }
}
