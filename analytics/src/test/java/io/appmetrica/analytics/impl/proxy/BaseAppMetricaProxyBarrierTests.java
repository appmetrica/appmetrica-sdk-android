package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.DeeplinkConsumer;
import io.appmetrica.analytics.impl.MainReporter;
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider;
import io.appmetrica.analytics.impl.proxy.validation.ActivationValidator;
import io.appmetrica.analytics.impl.proxy.validation.Barrier;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BaseAppMetricaProxyBarrierTests extends CommonTest {

    @Mock
    protected AppMetricaFacadeProvider mProvider;
    @Mock
    protected ActivationValidator mActivationValidator;
    @Mock
    protected MainReporter mMainReporter;
    @Mock
    protected MainReporterApiConsumerProvider mainReporterApiConsumerProvider;
    @Mock
    protected AppMetricaFacade mImpl;

    protected BaseAppMetricaProxy mProxy;
    protected Barrier mBarrier;

    protected String mName;
    protected Class<?>[] mArgs;
    protected boolean mIfNoArgs;

    public static Collection<Object[]> data(@NonNull List<String> methodsNotToCheck,
                                            @NonNull List<String> methodsWithNoArguments,
                                            Class clazz) {
        ArrayList<String> ignoredMethods = new ArrayList<>(methodsNotToCheck);
        ignoredMethods.add("$jacocoInit"); //jacoco adds extra methods, so otherwise build will fail on TeamCity
        ArrayList<Object[]> data = new ArrayList<Object[]>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (methodsWithNoArguments.contains(method.getName())) {
                    data.add(new Object[]{
                            method.getName(), true, method.getParameterTypes()
                        }
                    );
                } else if (!ignoredMethods.contains(method.getName())) {
                    data.add(new Object[]{
                        method.getName(), false, method.getParameterTypes()
                    });
                }
            }
        }
        return data;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mainReporterApiConsumerProvider).when(mImpl).getMainReporterApiConsumerProvider();
        doReturn(mMainReporter).when(mainReporterApiConsumerProvider).getMainReporter();
        doReturn(mock(DeeplinkConsumer.class)).when(mainReporterApiConsumerProvider).getDeeplinkConsumer();
        doReturn(mImpl).when(mProvider).peekInitializedImpl();
        doReturn(mImpl).when(mProvider).getInitializedImpl(any(Context.class));
    }

    public BaseAppMetricaProxyBarrierTests(String name, boolean ifNoArgs, Class<?>[] args) {
        mName = name;
        mArgs = args;
        mIfNoArgs = ifNoArgs;
    }

    public void testCallBarrier()
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (mIfNoArgs) {
            CallProxyVerifier.verifyNoArgsForMock(mProxy, mBarrier, mName, mArgs);
        } else {
            CallProxyVerifier.verify(mProxy, mBarrier, mName, mArgs);
        }
    }
}
