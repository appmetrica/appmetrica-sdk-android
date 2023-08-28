package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.impl.ecommerce.ECommerceEventProvider;
import io.appmetrica.analytics.impl.ecommerce.client.converter.CartActionInfoEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
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
 * @see ECommerceEventProvider#addCartItemEvent(ECommerceCartItem)
 * @see ECommerceEventProvider#removeCartItemEvent(ECommerceCartItem)
 */
@RunWith(RobolectricTestRunner.class)
public class CartActionEventTest extends CommonTest {

    @Mock
    private ECommerceEventConverter<CartActionInfoEvent> converter;
    @Mock
    private CartItemWrapper cartItemWrapper;
    @Mock
    private List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> proto;
    @Mock
    private ECommerceCartItem eCommerceCartItem;
    @Mock
    private ECommerceProduct eCommerceProduct;
    @Mock
    private ECommercePrice eCommerceRevenue;
    @Mock
    private ECommerceAmount eCommerceAmount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(eCommerceCartItem.getProduct()).thenReturn(eCommerceProduct);
        when(eCommerceCartItem.getRevenue()).thenReturn(eCommerceRevenue);
        when(eCommerceRevenue.getFiat()).thenReturn(eCommerceAmount);
    }

    @Test
    public void constructorFromDefaultConstructor() {
        assertThat(new CartActionInfoEvent(10, eCommerceCartItem).getConverter())
                .isInstanceOf(CartActionInfoEventConverter.class);
    }

    @Test
    public void toProto() {
        int eventType = 34;
        CartActionInfoEvent cartActionInfoEvent = new CartActionInfoEvent(eventType, cartItemWrapper, converter);
        when(converter.fromModel(cartActionInfoEvent)).thenReturn(proto);
        assertThat(cartActionInfoEvent.toProto()).isEqualTo(proto);
    }

    @Test
    public void getPublicDescriptionForAddCartItem() {
        CartActionInfoEvent event =
                new CartActionInfoEvent(CartActionInfoEvent.EVENT_TYPE_ADD_TO_CART, cartItemWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("add cart item info");
    }

    @Test
    public void getPublicDescriptionForRemoveCartItem() {
        CartActionInfoEvent event =
                new CartActionInfoEvent(CartActionInfoEvent.EVENT_TYPE_REMOVE_FROM_CART, cartItemWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("remove cart item info");
    }

    @Test
    public void getPublicDescriptionForUnknownType() {
        CartActionInfoEvent event = new CartActionInfoEvent(999, cartItemWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("unknown cart action info");
    }
}
