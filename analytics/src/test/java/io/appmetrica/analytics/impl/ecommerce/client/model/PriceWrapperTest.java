package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import io.appmetrica.analytics.impl.ecommerce.ECommerceEventProviderTest;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @see ECommerceEventProviderTest#orderEvent()
 * @see ECommerceEventProviderTest#addCartItemEvent()
 * @see ECommerceEventProviderTest#removeCartItemEvent()
 * @see ECommerceEventProviderTest#showProductDetailsEvent()
 * @see ECommerceEventProviderTest#showProductCardEvent()
 */
@RunWith(RobolectricTestRunner.class)
public class PriceWrapperTest extends CommonTest {

    @Test
    public void constructorIfInternalComponentsIsNull() {
        PriceWrapper priceWrapper = new PriceWrapper(
            new ECommercePrice(new ECommerceAmount(BigDecimal.TEN, "USD"))
        );
        assertThat(priceWrapper.internalComponents).isNull();
    }

}
