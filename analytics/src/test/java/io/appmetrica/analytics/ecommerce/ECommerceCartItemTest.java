package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ECommerceCartItemQuantityTest
 */
@RunWith(RobolectricTestRunner.class)
public class ECommerceCartItemTest extends CommonTest {

    @Mock
    private ECommerceProduct product;
    @Mock
    private BigDecimal quantity;
    @Mock
    private ECommercePrice revenue;
    @Mock
    private ECommerceReferrer referrer;
    @Mock
    private ECommerceReferrer secondReferrer;

    private ECommerceCartItem cartItem;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        cartItem = new ECommerceCartItem(product, revenue, quantity);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceCartItem> assertions =
                ObjectPropertyAssertions(cartItem)
                        .withFinalFieldOnly(false)
                        .withDeclaredAccessibleFields(true);

        assertions.checkField("product", "getProduct", product);
        assertions.checkField("revenue", "getRevenue", revenue);
        assertions.checkField("quantity", "getQuantity", quantity);
        assertions.checkField("referrer", "getReferrer", null);

        assertions.checkAll();
    }

    @Test
    public void setReferrer() {
        cartItem.setReferrer(referrer);
        assertThat(cartItem.getReferrer()).isEqualTo(referrer);
    }

    @Test
    public void setReferrerTwice() {
        cartItem.setReferrer(referrer);
        cartItem.setReferrer(secondReferrer);
        assertThat(cartItem.getReferrer()).isEqualTo(secondReferrer);
    }

    @Test
    public void setNullReferrer() {
        cartItem.setReferrer(null);
        assertThat(cartItem.getReferrer()).isNull();
    }

    @Test
    public void setNullReferrerAfterNonNull() {
        cartItem.setReferrer(referrer);
        cartItem.setReferrer(null);
        assertThat(cartItem.getReferrer()).isNull();
    }
}
