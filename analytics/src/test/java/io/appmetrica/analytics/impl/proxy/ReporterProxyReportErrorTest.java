package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.proxy.synchronous.ReporterSynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.ReporterBarrier;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterProxyReportErrorTest extends CommonTest {

    private static final String API_KEY = TestsData.generateApiKey();

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Mock
    private ReporterSynchronousStageExecutor mSynchronousStageExecutor;
    @Mock
    private ReporterBarrier mBarrier;
    @Mock
    private AppMetricaFacadeProvider mProvider;
    @Mock
    private AppMetricaFacade mImpl;
    @Mock
    private IReporterExtended mReporter;
    private ReporterConfig mConfig = ReporterConfig.newConfigBuilder(API_KEY).build();
    final IHandlerExecutor mExecutor = new StubbedBlockingExecutor();

    private ReporterExtendedProxy mReporterProxy;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mImpl).when(mProvider).getInitializedImpl(any(Context.class));
        doReturn(mReporter).when(mImpl).getReporter(argThat(new ReporterInternalConfigArgumentMatcher(API_KEY)));
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(mExecutor);
        mReporterProxy = new ReporterExtendedProxy(
                RuntimeEnvironment.getApplication(),
                mBarrier,
                mProvider,
                mSynchronousStageExecutor,
                mConfig,
                mock(PluginReporterProxy.class)
        );
    }

    @Test
    public void testReportError() {
        String message = "message";
        Throwable originalThrowable = mock(Throwable.class);
        Throwable newThrowable = mock(Throwable.class);
        when(mSynchronousStageExecutor.reportError(message, originalThrowable)).thenReturn(newThrowable);
        mReporterProxy.reportError(message, originalThrowable);
        verify(mReporter).reportError(message, newThrowable);
    }
}
