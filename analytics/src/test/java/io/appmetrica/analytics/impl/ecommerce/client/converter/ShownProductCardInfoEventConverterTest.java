package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent;
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
public class ShownProductCardInfoEventConverterTest extends CommonTest {

    @Mock
    private ScreenConverter screenConverter;
    @Mock
    private ProductConverter productConverter;
    @Mock
    private ScreenWrapper screen;
    @Mock
    private Ecommerce.ECommerceEvent.Screen screenProto;
    @Mock
    private ProductWrapper product;
    @Mock
    private Ecommerce.ECommerceEvent.Product productProto;

    private ShownProductCardInfoEventConverter shownProductCardInfoEventConverter;

    private final int productBytesTruncated = 1;
    private final int screenBytesTruncated = 10;

    private final int totalBytesTruncated = productBytesTruncated + screenBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(screenConverter.fromModel(screen))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>(
                        screenProto,
                        new BytesTruncatedInfo(screenBytesTruncated)
                ));

        when(productConverter.fromModel(product))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>(
                        productProto,
                        new BytesTruncatedInfo(productBytesTruncated)
                ));

        shownProductCardInfoEventConverter = new ShownProductCardInfoEventConverter(screenConverter, productConverter);
    }

    @Test
    public void constructor() throws Exception {
        shownProductCardInfoEventConverter = new ShownProductCardInfoEventConverter();

        ObjectPropertyAssertions<ShownProductCardInfoEventConverter> assertions =
                ObjectPropertyAssertions(shownProductCardInfoEventConverter)
                        .withPrivateFields(true);

        assertions.checkFieldNonNull("screenConverter");
        assertions.checkFieldNonNull("productConverter");

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        ShownProductCardInfoEvent event = new ShownProductCardInfoEvent(product, screen, shownProductCardInfoEventConverter);

        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos =
                shownProductCardInfoEventConverter.fromModel(event);

        Ecommerce.ECommerceEvent.ShownProductCardInfo expectedShowProductCardInfo =
                new Ecommerce.ECommerceEvent.ShownProductCardInfo();
        expectedShowProductCardInfo.product = productProto;
        expectedShowProductCardInfo.screen = screenProto;

        assertThat(protos.size()).isEqualTo(1);

        ObjectPropertyAssertions(protos.get(0))
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(totalBytesTruncated)
                )
                .checkFieldRecursively(
                        "result",
                        new ECommerceEventAssertionsConsumer(
                                Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_CARD
                        ).setExpectedShowProductCardInfo(expectedShowProductCardInfo)
                )
                .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        shownProductCardInfoEventConverter.toModel(
                Collections.<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>emptyList()
        );
    }
}
