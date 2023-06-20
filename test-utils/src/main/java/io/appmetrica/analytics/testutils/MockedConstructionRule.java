package io.appmetrica.analytics.testutils;

import org.junit.rules.ExternalResource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class MockedConstructionRule<T> extends ExternalResource {

    private MockedConstruction<T> mock;

    private final Class<T> clazz;
    private final ConstructionArgumentCaptor<T> interceptor;

    public MockedConstructionRule(Class<T> clazz) {
        this(clazz, null);
    }

    public MockedConstructionRule(Class<T> clazz, MockedConstruction.MockInitializer<T> initializer) {
        this.clazz = clazz;
        if (initializer == null) {
            interceptor = new ConstructionArgumentCaptor<>();
        } else {
            interceptor = new ConstructionArgumentCaptor<>(initializer);
        }
    }

    public MockedConstruction<T> getConstructionMock() {
        return mock;
    }

    public ConstructionArgumentCaptor<T> getArgumentInterceptor() {
        return interceptor;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        mock = Mockito.mockConstruction(clazz, interceptor);
    }

    @Override
    protected void after() {
        super.after();
        mock.close();
    }

}
