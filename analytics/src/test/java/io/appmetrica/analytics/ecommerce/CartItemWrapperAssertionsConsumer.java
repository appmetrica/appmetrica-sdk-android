package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import java.math.BigDecimal;
import java.util.function.Consumer;

class CartItemWrapperAssertionsConsumer implements Consumer<ObjectPropertyAssertions<CartItemWrapper>> {

    private ProductWrapperAssertionsConsumer expectedProduct;
    private BigDecimal expectedQuantity;
    private PriceWrapperAssertionConsumer expectedRevenue;
    private ReferrerWrapperAssertionsConsumer expectedReferrer;

    @Override
    public void accept(ObjectPropertyAssertions<CartItemWrapper> assertions) {
        try {
            assertions.checkFieldRecursively("product", expectedProduct);
            assertions.checkField("quantity", expectedQuantity);
            assertions.checkFieldRecursively("revenue", expectedRevenue);
            assertions.checkFieldRecursively("referrer", expectedReferrer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CartItemWrapperAssertionsConsumer setExpectedProduct(ProductWrapperAssertionsConsumer expectedProduct) {
        this.expectedProduct = expectedProduct;
        return this;
    }

    public CartItemWrapperAssertionsConsumer setExpectedQuantity(BigDecimal expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
        return this;
    }

    public CartItemWrapperAssertionsConsumer setExpectedRevenue(PriceWrapperAssertionConsumer expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
        return this;
    }

    public CartItemWrapperAssertionsConsumer setExpectedReferrer(ReferrerWrapperAssertionsConsumer expectedReferrer) {
        this.expectedReferrer = expectedReferrer;
        return this;
    }
}
