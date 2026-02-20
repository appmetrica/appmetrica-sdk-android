package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class ECommerceOrderTest extends CommonTest {

    private String identifier = "Order identifier";
    @Mock
    private List<ECommerceCartItem> cartItems;
    @Mock
    private Map<String, String> payload;
    @Mock
    private Map<String, String> secondPayload;

    private ECommerceOrder order;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        order = new ECommerceOrder(identifier, cartItems);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceOrder> assertions = ObjectPropertyAssertions(order)
                .withFinalFieldOnly(false)
                .withDeclaredAccessibleFields(true);

        assertions.checkField("identifier", "getIdentifier", identifier);
        assertions.checkField("cartItems", "getCartItems", cartItems);
        assertions.checkField("payload", "getPayload", null);

        assertions.checkAll();
    }

    @Test
    public void setPayload() {
        order.setPayload(payload);
        assertThat(order.getPayload()).isEqualTo(payload);
    }

    @Test
    public void setPayloadTwice() {
        order.setPayload(payload);
        order.setPayload(secondPayload);
        assertThat(order.getPayload()).isEqualTo(secondPayload);
    }

    @Test
    public void setNullPayload() {
        order.setPayload(null);
        assertThat(order.getPayload()).isNull();
    }

    @Test
    public void setNullPayloadAfterNonNull() {
        order.setPayload(payload);
        order.setPayload(null);
        assertThat(order.getPayload()).isNull();
    }
}
