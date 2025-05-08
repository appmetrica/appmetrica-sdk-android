package io.appmetrica.analytics.impl.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderWrapper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

class OrderWrapperAssertionsConsumer implements Consumer<ObjectPropertyAssertions<OrderWrapper>> {

    private String expectedIdentifier;
    private Map<String, String> expectedPayload;
    private List<CartItemWrapperAssertionsConsumer> expectedCartItems;

    @Override
    public void accept(ObjectPropertyAssertions<OrderWrapper> assertions) {
        try {
            assertions.checkFieldNonNull("uuid");
            assertions.checkField("identifier", expectedIdentifier);
            assertions.checkField("payload", expectedPayload);
            assertions.checkFieldMatchPredicate("cartItems", new Predicate<List<CartItemWrapper>>() {
                @Override
                public boolean test(List<CartItemWrapper> cartItemWrappers) {
                    if (expectedCartItems == null) {
                        assertThat(cartItemWrappers).isNull();
                    } else if (expectedCartItems.size() == 0) {
                        assertThat(cartItemWrappers).isEmpty();
                    } else {
                        assertThat(cartItemWrappers).hasSize(expectedCartItems.size());
                        for (int i = 0; i < cartItemWrappers.size(); i++) {
                            ObjectPropertyAssertions<CartItemWrapper> cartItemAssertions =
                                ObjectPropertyAssertions(cartItemWrappers.get(i));
                            expectedCartItems.get(i).accept(cartItemAssertions);
                            cartItemAssertions.checkAll();
                        }
                    }

                    return true;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OrderWrapperAssertionsConsumer setExpectedIdentifier(String expectedIdentifier) {
        this.expectedIdentifier = expectedIdentifier;
        return this;
    }

    public OrderWrapperAssertionsConsumer setExpectedPayload(Map<String, String> expectedPayload) {
        this.expectedPayload = expectedPayload;
        return this;
    }

    public OrderWrapperAssertionsConsumer setExpectedCartItems(List<CartItemWrapperAssertionsConsumer> expectedCartItems) {
        this.expectedCartItems = expectedCartItems;
        return this;
    }
}
