package io.appmetrica.analytics.impl.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class PriceWrapperAssertionConsumer implements Consumer<ObjectPropertyAssertions<PriceWrapper>> {

    private AmountWrapper expectedFiat;
    private List<AmountWrapper> expectedInternalComponents;

    @Override
    public void accept(ObjectPropertyAssertions<PriceWrapper> assertions) {
        try {
            assertions.checkFieldComparingFieldByField("fiat", expectedFiat);
            assertions.checkField("internalComponents", expectedInternalComponents, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PriceWrapperAssertionConsumer setExpectedFiat(ECommerceAmount amount) {
        expectedFiat = wrap(amount);
        return this;
    }

    public PriceWrapperAssertionConsumer setExpectedInternalComponents(List<ECommerceAmount> internalComponents) {
        if (internalComponents != null) {
            expectedInternalComponents = new ArrayList<AmountWrapper>(internalComponents.size());
            for (ECommerceAmount item : internalComponents) {
                expectedInternalComponents.add(wrap(item));
            }
        }
        return this;
    }

    private AmountWrapper wrap(ECommerceAmount eCommerceAmount) {
        return new AmountWrapper(eCommerceAmount.getAmount(), eCommerceAmount.getUnit());
    }
}
