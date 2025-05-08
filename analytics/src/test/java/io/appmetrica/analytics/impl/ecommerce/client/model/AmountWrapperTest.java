package io.appmetrica.analytics.impl.ecommerce.client.model;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AmountWrapperTest extends CommonTest {

    @Test
    public void constructor() throws Exception {
        BigDecimal amount = new BigDecimal("123142.343454");
        String unit = "USD";
        AmountWrapper amountWrapper = new AmountWrapper(new ECommerceAmount(amount, unit));

        ObjectPropertyAssertions<AmountWrapper> assertions =
            ObjectPropertyAssertions(amountWrapper);

        assertions.checkField("amount", amount);
        assertions.checkField("unit", unit);

        assertions.checkAll();
    }

}
