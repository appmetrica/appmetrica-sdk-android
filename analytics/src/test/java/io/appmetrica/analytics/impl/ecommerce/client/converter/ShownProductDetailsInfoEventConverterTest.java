package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
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
public class ShownProductDetailsInfoEventConverterTest extends CommonTest {

    @Mock
    private ProductConverter productConverter;
    @Mock
    private ReferrerConverter referrerConverter;
    @Mock
    private ProductWrapper product;
    @Mock
    private Ecommerce.ECommerceEvent.Product productProto;
    @Mock
    private ReferrerWrapper referrer;
    @Mock
    private Ecommerce.ECommerceEvent.Referrer referrerProto;

    private ShownProductDetailsInfoEventConverter shownProductDetailsInfoEventConverter;

    private final int productBytesTruncated = 1;
    private final int referrerBytesTruncated = 10;

    private final int totalBytesTruncated = productBytesTruncated + referrerBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(productConverter.fromModel(product))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>(
                        productProto,
                        new BytesTruncatedInfo(productBytesTruncated)
                ));
        when(referrerConverter.fromModel(referrer))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider>(
                        referrerProto,
                        new BytesTruncatedInfo(referrerBytesTruncated)
                ));

        shownProductDetailsInfoEventConverter = new ShownProductDetailsInfoEventConverter(productConverter, referrerConverter);
    }

    @Test
    public void constructor() throws Exception {
        shownProductDetailsInfoEventConverter = new ShownProductDetailsInfoEventConverter();

        ObjectPropertyAssertions<ShownProductDetailsInfoEventConverter> assertions =
                ObjectPropertyAssertions(shownProductDetailsInfoEventConverter)
                        .withPrivateFields(true);

        assertions.checkFieldNonNull("referrerConverter");
        assertions.checkFieldNonNull("productConverter");

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        ShownProductDetailInfoEvent event =
                new ShownProductDetailInfoEvent(product, referrer, shownProductDetailsInfoEventConverter);
        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos =
                shownProductDetailsInfoEventConverter.fromModel(event);

        Ecommerce.ECommerceEvent.ShownProductDetailsInfo expectedShownProductDetailsInfo =
                new Ecommerce.ECommerceEvent.ShownProductDetailsInfo();
        expectedShownProductDetailsInfo.product = productProto;
        expectedShownProductDetailsInfo.referrer = referrerProto;

        assertThat(protos.size()).isEqualTo(1);

        ObjectPropertyAssertions(protos.get(0))
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(totalBytesTruncated)
                )
                .checkFieldRecursively(
                        "result",
                        new ECommerceEventAssertionsConsumer(
                                Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_PRODUCT_DETAILS
                        ).setExpectedShowProductDetailsInfo(expectedShownProductDetailsInfo)
                )
                .checkAll();
    }

    @Test
    public void toProtoWithNullReferrer() {
        ShownProductDetailInfoEvent event =
                new ShownProductDetailInfoEvent(product, null, shownProductDetailsInfoEventConverter);
        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos =
                shownProductDetailsInfoEventConverter.fromModel(event);

        assertThat(protos.size()).isEqualTo(1);

        SoftAssertions assertions = new SoftAssertions();

        assertions.assertThat(protos.get(0).getBytesTruncated())
                .as("Total bytes truncated")
                .isEqualTo(productBytesTruncated);

        assertions.assertThat(protos.get(0).result.shownProductDetailsInfo.referrer).as("Referrer").isNull();

        assertions.assertAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        shownProductDetailsInfoEventConverter.toModel(
                Collections.<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>emptyList()
        );
    }
}
