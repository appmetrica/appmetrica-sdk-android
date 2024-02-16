package io.appmetrica.analytics.impl.proxy;

import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.Barrier;
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AppMetricaProxyBarrierTests extends BaseAppMetricaProxyBarrierTests {

    private static final List<String> methodsNotToCheck = Arrays.asList(
        "getDeviceId"
    );

    private static final List<String> methodsWithNoArguments = Arrays.asList(
        "pauseSession",
        "resumeSession"
    );

    @Mock
    private ReporterProxyStorage mReporterProxyStorage;
    @Mock
    private SilentActivationValidator silentActivationValidator;

    @ParameterizedRobolectricTestRunner.Parameters(name = "Test if {0} is called")
    public static Collection<Object[]> data() {
        return data(
                methodsNotToCheck,
                methodsWithNoArguments,
                AppMetricaProxy.class
        );
    }

    @Before
    public void setUp() {
        super.setUp();
        mBarrier = mock(Barrier.class);
        when(silentActivationValidator.validate()).thenReturn(ValidationResult.successful(mock(Validator.class)));
        mProxy = new AppMetricaProxy(
                mProvider,
                new StubbedBlockingExecutor(),
                mBarrier,
                silentActivationValidator,
                mock(WebViewJsInterfaceHandler.class),
                mock(SynchronousStageExecutor.class),
                mReporterProxyStorage,
                mock(DefaultOneShotMetricaConfig.class),
                mock(SessionsTrackingManager.class)
        );
    }

    public AppMetricaProxyBarrierTests(String name, boolean ifNoArgs, Class<?>[] args) {
        super(name, ifNoArgs, args);
    }

    @Test
    public void testCallBarrier() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        super.testCallBarrier();
    }
}
