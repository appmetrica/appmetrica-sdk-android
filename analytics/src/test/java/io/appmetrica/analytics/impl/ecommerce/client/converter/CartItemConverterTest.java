package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CartItemConverterTest extends CommonTest {

    @Mock
    private ProductConverter productConverter;
    @Mock
    private DecimalConverter decimalConverter;
    @Mock
    private PriceConverter priceConverter;
    @Mock
    private ReferrerConverter referrerConverter;

    @Mock
    private ProductWrapper productWrapper;
    @Mock
    private Ecommerce.ECommerceEvent.Product productProto;
    @Mock
    private BigDecimal quantity;
    @Mock
    private Ecommerce.ECommerceEvent.Decimal quantityProto;
    @Mock
    private PriceWrapper revenue;
    @Mock
    private Ecommerce.ECommerceEvent.Price revenueProto;
    @Mock
    private ReferrerWrapper referrerWrapper;
    @Mock
    private Ecommerce.ECommerceEvent.Referrer referrerProto;

    private CartItemConverter cartItemConverter;

    private final int revenueBytesTruncated = 1;
    private final int productBytesTruncated = 10;
    private final int referrerBytesTruncated = 100;

    private final int totalBytesTruncated = revenueBytesTruncated + productBytesTruncated + referrerBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(productConverter.fromModel(productWrapper))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Product, BytesTruncatedProvider>(
                        productProto,
                        new BytesTruncatedInfo(productBytesTruncated)
                ));
        when(decimalConverter.fromModel(quantity)).thenReturn(quantityProto);
        when(priceConverter.fromModel(revenue))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Price, BytesTruncatedProvider>(
                        revenueProto,
                        new BytesTruncatedInfo(revenueBytesTruncated)
                ));
        when(referrerConverter.fromModel(referrerWrapper))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Referrer, BytesTruncatedProvider>(
                        referrerProto,
                        new BytesTruncatedInfo(referrerBytesTruncated)
                ));

        cartItemConverter =
                new CartItemConverter(productConverter, decimalConverter, priceConverter, referrerConverter);
    }

    @Test
    public void constructor() throws Exception {
        cartItemConverter = new CartItemConverter();

        ObjectPropertyAssertions<CartItemConverter> assertions =
                ObjectPropertyAssertions(cartItemConverter)
                        .withPrivateFields(true);

        assertions.checkFieldNonNull("productConverter");
        assertions.checkFieldNonNull("decimalConverter");
        assertions.checkFieldNonNull("priceConverter");
        assertions.checkFieldNonNull("referrerConverter");

        assertions.checkAll();
    }

    @Test
    public void toProto() throws Exception {
        CartItemWrapper cartItemWrapper =
                new CartItemWrapper(productWrapper, quantity, revenue, referrerWrapper);

        Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider> result =
                cartItemConverter.fromModel(cartItemWrapper);

        assertThatCartItemResultMatchExpectedValues(
                result,
                totalBytesTruncated,
                productProto,
                quantityProto,
                revenueProto,
                referrerProto
        );
    }

    @Test
    public void toProtoForNullableReferrer() throws Exception {
        CartItemWrapper cartItemWrapper =
                new CartItemWrapper(productWrapper, quantity, revenue, null);

        assertThatCartItemResultMatchExpectedValues(
                cartItemConverter.fromModel(cartItemWrapper),
                totalBytesTruncated - referrerBytesTruncated,
                productProto,
                quantityProto,
                revenueProto,
                null
        );
    }

    private void assertThatCartItemResultMatchExpectedValues(
            Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider> result,
            int expectedBytesTruncated,
            Ecommerce.ECommerceEvent.Product expectedProduct,
            Ecommerce.ECommerceEvent.Decimal expectedQuantity,
            Ecommerce.ECommerceEvent.Price expectedRevenue,
            Ecommerce.ECommerceEvent.Referrer expectedReferrer
    ) throws Exception {

        Ecommerce.ECommerceEvent.CartItem expectedCartItem = new Ecommerce.ECommerceEvent.CartItem();
        expectedCartItem.product = expectedProduct;
        expectedCartItem.quantity = expectedQuantity;
        expectedCartItem.revenue = expectedRevenue;
        expectedCartItem.referrer = expectedReferrer;

        ObjectPropertyAssertions<Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>> assertions =
                ObjectPropertyAssertions(result);

        assertions.checkFieldRecursively(
                "metaInfo", new TruncationInfoConsumer(expectedBytesTruncated)
        );
        assertions.checkFieldComparingFieldByField("result", expectedCartItem);

        assertions.checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        cartItemConverter.toModel(new Result<Ecommerce.ECommerceEvent.CartItem, BytesTruncatedProvider>(
                new Ecommerce.ECommerceEvent.CartItem(), new BytesTruncatedInfo(0)
        ));
    }
}
