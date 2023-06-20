package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import java.util.function.Consumer;

class ReferrerWrapperAssertionsConsumer implements Consumer<ObjectPropertyAssertions<ReferrerWrapper>> {

    private String expectedType;
    private String expectedId;
    private ScreenWrapperAssertionsConsumer expectedScreen;

    @Override
    public void accept(ObjectPropertyAssertions<ReferrerWrapper> assertions) {
        try {
            assertions.checkField("type", expectedType);
            assertions.checkField("identifier", expectedId);
            assertions.checkFieldRecursively("screen", expectedScreen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReferrerWrapperAssertionsConsumer setExpectedType(String expectedType) {
        this.expectedType = expectedType;
        return this;
    }

    public ReferrerWrapperAssertionsConsumer setExpectedId(String expectedId) {
        this.expectedId = expectedId;
        return this;
    }

    public ReferrerWrapperAssertionsConsumer setExpectedScreen(ScreenWrapperAssertionsConsumer expectedScreen) {
        this.expectedScreen = expectedScreen;
        return this;
    }
}
