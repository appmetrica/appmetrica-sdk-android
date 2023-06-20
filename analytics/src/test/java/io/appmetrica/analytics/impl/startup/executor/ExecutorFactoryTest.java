package io.appmetrica.analytics.impl.startup.executor;

import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ExecutorFactoryTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} will produce {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{StubbedExecutorFactory.class, StubbedStartupExecutor.class, null, null},
                new Object[]{RegularExecutorFactory.class, RegularStartupExecutor.class, StartupUnit.class, mock(StartupUnit.class)}
        );
    }

    private final ComponentStartupExecutorFactory mFactory;
    private final Class<? extends StartupExecutor> mExecutorClass;

    public ExecutorFactoryTest(Class<ComponentStartupExecutorFactory> factory, Class<? extends StartupExecutor> executorClass, Class<Object> argumentClass, Object argument) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (argument == null) {
            mFactory = factory.newInstance();
        } else {
            mFactory = factory.getConstructor(argumentClass).newInstance(argument);
        }
        mExecutorClass = executorClass;
    }

    @Test
    public void test() {
        assertThat(mFactory.create()).isExactlyInstanceOf(mExecutorClass);
    }

}
