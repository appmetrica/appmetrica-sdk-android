package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class ScreenWrapperAssertionsConsumer implements Consumer<ObjectPropertyAssertions<ScreenWrapper>> {

    private List<String> expectedCategoriesPath;
    private String expectedName;
    private Map<String, String> expectedPayload;
    private String expecredSearchQuery;

    @Override
    public void accept(ObjectPropertyAssertions<ScreenWrapper> assertions) {
        try {
            assertions.checkField("categoriesPath", expectedCategoriesPath, true);
            assertions.checkField("name", expectedName);
            assertions.checkField("payload", expectedPayload);
            assertions.checkField("searchQuery", expecredSearchQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ScreenWrapperAssertionsConsumer setExpectedCategoriesPath(List<String> expectedCategoriesPath) {
        this.expectedCategoriesPath = expectedCategoriesPath;
        return this;
    }

    public ScreenWrapperAssertionsConsumer setExpectedName(String expectedName) {
        this.expectedName = expectedName;
        return this;
    }

    public ScreenWrapperAssertionsConsumer setExpectedPayload(Map<String, String> expectedPayload) {
        this.expectedPayload = expectedPayload;
        return this;
    }

    public ScreenWrapperAssertionsConsumer setExpecredSearchQuery(String expecredSearchQuery) {
        this.expecredSearchQuery = expecredSearchQuery;
        return this;
    }
}
