package io.appmetrica.analytics.ecommerce;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ECommerceCartItemQuantityTest
 */
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
                Assertions.INSTANCE.ObjectPropertyAssertions(cartItem)
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
