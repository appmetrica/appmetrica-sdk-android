package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ECommerceEventTest extends CommonTest {

    @Mock
    private ECommerceEvent event;
    @Mock
    private ECommerceEventProvider provider;
    @Mock
    private ECommerceProduct product;
    @Mock
    private ECommerceScreen screen;
    @Mock
    private ECommerceReferrer referrer;
    @Mock
    private ECommerceCartItem cartItem;
    @Mock
    private ECommerceOrder order;

    private ECommerceEventProvider originalProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        originalProvider = ECommerceEvent.getProvider();
        ECommerceEvent.setProvider(provider);
    }

    @After
    public void tearDown() throws Exception {
        ECommerceEvent.setProvider(originalProvider);
    }

    @Test
    public void showScreenEvent() {
        when(provider.showScreenEvent(screen)).thenReturn(event);
        assertThat(ECommerceEvent.showScreenEvent(screen)).isEqualTo(event);
    }

    @Test
    public void showProductCardEvent() {
        when(provider.showProductCardEvent(product, screen)).thenReturn(event);
        assertThat(ECommerceEvent.showProductCardEvent(product, screen)).isEqualTo(event);
    }

    @Test
    public void showProductDetailsWithNullReferrer() {
        when(provider.showProductDetailsEvent(product, null)).thenReturn(event);
        assertThat(ECommerceEvent.showProductDetailsEvent(product, null)).isEqualTo(event);
    }

    @Test
    public void showProductDetailsWithReferrer() {
        when(provider.showProductDetailsEvent(product, referrer)).thenReturn(event);
        assertThat(ECommerceEvent.showProductDetailsEvent(product, referrer)).isEqualTo(event);
    }

    @Test
    public void addCartItem() {
        when(provider.addCartItemEvent(cartItem)).thenReturn(event);
        assertThat(ECommerceEvent.addCartItemEvent(cartItem)).isEqualTo(event);
    }

    @Test
    public void removeCartItem() {
        when(provider.removeCartItemEvent(cartItem)).thenReturn(event);
        assertThat(ECommerceEvent.removeCartItemEvent(cartItem)).isEqualTo(event);
    }

    @Test
    public void beginCheckoutEvent() {
        when(provider.beginCheckoutEvent(order)).thenReturn(event);
        assertThat(ECommerceEvent.beginCheckoutEvent(order)).isEqualTo(event);
    }

    @Test
    public void purchaseEvent() {
        when(provider.purchaseEvent(order)).thenReturn(event);
        assertThat(ECommerceEvent.purchaseEvent(order)).isEqualTo(event);
    }

    @Test
    public void getPublicDescription() {
        assertThat(new ECommerceEvent() {
            @Override
            public List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> toProto() {
                return null;
            }
        }.getPublicDescription()).isEqualTo("E-commerce base event");
    }
}
