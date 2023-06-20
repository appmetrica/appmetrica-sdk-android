package io.appmetrica.analytics.testutils;

import org.junit.rules.ExternalResource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class MockedStaticRule<T> extends ExternalResource {

    private final Class<T> classToMock;
    private MockedStatic<T> staticMock;

    public MockedStaticRule(Class<T> classToMock) {
        this.classToMock = classToMock;
    }

    public MockedStatic<T> getStaticMock() {
        return staticMock;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        staticMock = Mockito.mockStatic(classToMock);
    }

    @Override
    protected void after() {
        super.after();
        staticMock.close();
    }
}
