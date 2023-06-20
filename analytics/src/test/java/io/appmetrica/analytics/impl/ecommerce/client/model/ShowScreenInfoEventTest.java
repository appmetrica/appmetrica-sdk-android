package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceEventProviderTest;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ShownScreenInfoEventConverter;
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
 * @see ECommerceEventProviderTest#showScreenEvent()
 */
@RunWith(RobolectricTestRunner.class)
public class ShowScreenInfoEventTest extends CommonTest {

    @Mock
    private ScreenWrapper screenWrapper;
    @Mock
    private ECommerceEventConverter<ShownScreenInfoEvent> converter;
    @Mock
    private List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> proto;
    @Mock
    private ECommerceScreen eCommerceScreen;

    private ShownScreenInfoEvent event;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void converterFromDefaultConstructor() {
        event = new ShownScreenInfoEvent(eCommerceScreen);
        assertThat(event.getConverter()).isInstanceOf(ShownScreenInfoEventConverter.class);
    }

    @Test
    public void toProto() {
        event = new ShownScreenInfoEvent(screenWrapper, converter);
        when(converter.fromModel(event)).thenReturn(proto);
        assertThat(event.toProto()).isEqualTo(proto);
    }

    @Test
    public void getPublicDescription() {
        event = new ShownScreenInfoEvent(screenWrapper, converter);

        assertThat(event.getPublicDescription()).isEqualTo("shown screen info");
    }
}
