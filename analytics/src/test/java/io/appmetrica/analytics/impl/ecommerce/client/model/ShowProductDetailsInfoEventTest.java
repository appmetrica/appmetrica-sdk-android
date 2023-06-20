package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceEventProviderTest;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownProductDetailsInfoEventConverter;
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
 * @see ECommerceEventProviderTest#showProductDetailsEvent()
 */
@RunWith(RobolectricTestRunner.class)
public class ShowProductDetailsInfoEventTest extends CommonTest {

    @Mock
    private ProductWrapper productWrapper;
    @Mock
    private ReferrerWrapper referrerWrapper;
    @Mock
    private ECommerceEventConverter<ShownProductDetailInfoEvent> converter;
    @Mock
    private List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> proto;
    @Mock
    private ECommerceProduct eCommerceProduct;
    @Mock
    private ECommerceReferrer eCommerceReferrer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void converterFromDefaultConstructor() {
        assertThat(new ShownProductDetailInfoEvent(eCommerceProduct, eCommerceReferrer).getConverter())
                .isInstanceOf(ShownProductDetailsInfoEventConverter.class);
    }

    @Test
    public void toProto() {
        ShownProductDetailInfoEvent event = new ShownProductDetailInfoEvent(productWrapper, referrerWrapper, converter);
        when(converter.fromModel(event)).thenReturn(proto);
        assertThat(event.toProto()).isEqualTo(proto);
    }

    @Test
    public void getPublicDescription() {
        ShownProductDetailInfoEvent event = new ShownProductDetailInfoEvent(productWrapper, referrerWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("shown product details info");
    }
}
