package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CartActionInfoEventConverterTest extends CommonTest {

    @Mock
    private CartItemConverter cartItemConverter;
    @Mock
    private CartItemWrapper cartItem;
    @Mock
    private Ecommerce.ECommerceEvent.CartItem cartItemProto;

    private CartActionInfoEventConverter cartActionInfoEventConverter;

    private final int cartItemBytesTruncated = 1000;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(cartItemConverter.fromModel(cartItem))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                        cartItemProto,
                        new BytesTruncatedInfo(cartItemBytesTruncated)
                ));

        cartActionInfoEventConverter = new CartActionInfoEventConverter(cartItemConverter);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<CartActionInfoEventConverter> assertions =
                ObjectPropertyAssertions(new CartActionInfoEventConverter())
                .withPrivateFields(true);

        assertions.checkFieldNonNull("cartItemConverter");

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        int eventType = 2;
        CartActionInfoEvent event = new CartActionInfoEvent(eventType, cartItem, cartActionInfoEventConverter);

        Ecommerce.ECommerceEvent.CartActionInfo expectedCartActionProto =
                new Ecommerce.ECommerceEvent.CartActionInfo();
        expectedCartActionProto.item = cartItemProto;

        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> results = cartActionInfoEventConverter.fromModel(event);

        assertThat(results.size()).isEqualTo(1);
        Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> result = cartActionInfoEventConverter.fromModel(event).get(0);

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> assertions =
                ObjectPropertyAssertions(result);

        assertions.checkFieldRecursively("metaInfo", new TruncationInfoConsumer(cartItemBytesTruncated));
        assertions.checkFieldRecursively(
                "result",
                new ECommerceEventAssertionsConsumer(eventType).setExpectedCartActionProto(expectedCartActionProto)
        );

        assertions.checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        cartActionInfoEventConverter.toModel(
                Collections.<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>emptyList()
        );
    }
}
