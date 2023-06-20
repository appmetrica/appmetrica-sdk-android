package io.appmetrica.analytics;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Currency;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class RevenueTest extends CommonTest {

    @Test
    public void testOnlyRequired() {
        Revenue revenue = Revenue.newBuilder(100, Currency.getInstance("USD")).build();
        assertThat(revenue.price).isEqualTo(100);
        assertThat(revenue.priceMicros).isNull();
        assertThat(revenue.currency.getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    public void testOnlyRequiredDecimal() {
        Revenue revenue = Revenue.newBuilder(55.5, Currency.getInstance("USD")).build();
        assertThat(revenue.price).isEqualTo(55.5);
        assertThat(revenue.priceMicros).isNull();
        assertThat(revenue.currency.getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    public void testOnlyRequiredMicros() {
        Revenue revenue = Revenue.newBuilderWithMicros(55500000, Currency.getInstance("USD")).build();
        assertThat(revenue.price).isNull();
        assertThat(revenue.priceMicros).isEqualTo(55500000);
        assertThat(revenue.currency.getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    public void testOptional() {
        Revenue.Receipt receipt = mock(Revenue.Receipt.class);
        Revenue revenue = Revenue.newBuilder(100, Currency.getInstance("USD"))
                .withPayload("payload")
                .withProductID("productID")
                .withQuantity(300)
                .withReceipt(receipt)
                .build();
        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(revenue.payload).as("payload").isEqualTo("payload");
        assertion.assertThat(revenue.productID).as("productID").isEqualTo("productID");
        assertion.assertThat(revenue.quantity).as("quantity").isEqualTo(300);
        assertion.assertThat(revenue.receipt).as("receipt").isSameAs(receipt);
        assertion.assertAll();
    }

    @Test(expected = ValidationException.class)
    public void testNullCurrency() {
        Revenue.newBuilder(1, null);
    }

}
