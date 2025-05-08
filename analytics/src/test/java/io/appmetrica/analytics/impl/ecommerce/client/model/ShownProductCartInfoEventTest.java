package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.ECommerceEventProviderTest;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownProductCardInfoEventConverter;
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
 * @see ECommerceEventProviderTest#showProductCardEvent()
 */
@RunWith(RobolectricTestRunner.class)
public class ShownProductCartInfoEventTest extends CommonTest {

    @Mock
    private ProductWrapper productWrapper;
    @Mock
    private ScreenWrapper screenWrapper;
    @Mock
    private ECommerceEventConverter<ShownProductCardInfoEvent> converter;
    @Mock
    private List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> proto;
    @Mock
    private ECommerceProduct eCommerceProduct;
    @Mock
    private ECommerceScreen eCommerceScreen;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void converterFromDefaultConstructor() {
        assertThat(new ShownProductCardInfoEvent(eCommerceProduct, eCommerceScreen).getConverter())
            .isInstanceOf(ShownProductCardInfoEventConverter.class);
    }

    @Test
    public void toProto() {
        ShownProductCardInfoEvent event = new ShownProductCardInfoEvent(productWrapper, screenWrapper, converter);
        when(converter.fromModel(event)).thenReturn(proto);
        assertThat(event.toProto()).isEqualTo(proto);
    }

    @Test
    public void getPublicDescription() {
        ShownProductCardInfoEvent event = new ShownProductCardInfoEvent(productWrapper, screenWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("shown product card info");
    }
}
