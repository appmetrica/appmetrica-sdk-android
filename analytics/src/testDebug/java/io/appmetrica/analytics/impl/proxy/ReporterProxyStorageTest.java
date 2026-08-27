package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.androidtestutils.rules.ContextRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterProxyStorageTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private Context mContext;
    private final String mApiKey = TestsData.generateApiKey();

    @Mock
    private IHandlerExecutor executor;

    private ReporterProxyStorage storage;

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Before
    public void setUp() {
        mContext = contextRule.getContext();
        MockitoAnnotations.openMocks(this);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()).thenReturn(executor);
        AppMetricaFacadeProvider facadeProvider = mock(AppMetricaFacadeProvider.class);
        when(facadeProvider.peekInitializedImpl()).thenReturn(mock(AppMetricaFacade.class));
        storage = new ReporterProxyStorage(facadeProvider);
    }

    @After
    public void tearDown() {
        AppMetricaFacade.killInstance();
    }

    @Test
    public void testGetOrCreateApiKey() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, mApiKey);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, mApiKey)).isEqualTo(proxy);
        assertThat(storage.getOrCreate(mContext, TestsData.generateApiKey())).isNotNull().isNotEqualTo(proxy);
    }

    @Test
    public void testGetOrCreateConfig() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(mApiKey).build();
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, config);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, config)).isEqualTo(proxy);
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(TestsData.generateApiKey()).build()))
            .isNotNull()
            .isNotEqualTo(proxy);
    }

    @Test
    public void testSameApiKeyThenConfig() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, mApiKey);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).build())).isEqualTo(proxy);
    }

    @Test
    public void testSameApiKeyDifferentConfigs() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).withDispatchPeriodSeconds(20).build());
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).withDispatchPeriodSeconds(10).withLogs().build())).isEqualTo(proxy);
    }

    @Test
    public void testConcurrentGetWaitsUntilActivationIsScheduled() throws Exception {
        CountDownLatch activationSchedulingStarted = new CountDownLatch(1);
        CountDownLatch allowActivationScheduling = new CountDownLatch(1);
        doAnswer(invocation -> {
            activationSchedulingStarted.countDown();
            if (!allowActivationScheduling.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Activation scheduling was not released");
            }
            return null;
        }).when(executor).execute(any(Runnable.class));

        FutureTask<ReporterExtendedProxy> firstGet = new FutureTask<>(
            () -> storage.getOrCreate(mContext, mApiKey)
        );
        Thread firstThread = new Thread(firstGet);
        firstThread.start();
        assertThat(activationSchedulingStarted.await(5, TimeUnit.SECONDS)).isTrue();

        FutureTask<ReporterExtendedProxy> secondGet = new FutureTask<>(
            () -> storage.getOrCreate(mContext, mApiKey)
        );
        Thread secondThread = new Thread(secondGet);
        secondThread.start();

        try {
            assertThreadState(secondThread, Thread.State.BLOCKED);
        } finally {
            allowActivationScheduling.countDown();
        }

        assertThat(secondGet.get(5, TimeUnit.SECONDS)).isSameAs(firstGet.get(5, TimeUnit.SECONDS));
        verify(executor).execute(any(Runnable.class));
    }

    @Test
    public void testProxyIsNotPublishedIfActivationSchedulingFails() {
        doThrow(new IllegalStateException("activation failure"))
            .doNothing()
            .when(executor)
            .execute(any(Runnable.class));

        assertThatThrownBy(() -> storage.getOrCreate(mContext, mApiKey))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("activation failure");

        assertThat(storage.getOrCreate(mContext, mApiKey)).isNotNull();
        verify(executor, times(2)).execute(any(Runnable.class));
    }

    @Test
    public void testDoesNotInitializeFacadeIfItWasInitializedBeforeScheduledTaskRuns() {
        AppMetricaFacadeProvider facadeProvider = mock(AppMetricaFacadeProvider.class);
        AppMetricaFacade facade = mock(AppMetricaFacade.class);
        when(facadeProvider.peekInitializedImpl())
            .thenReturn(null)
            .thenReturn(facade);
        ReporterProxyStorage storage = new ReporterProxyStorage(facadeProvider);
        final Runnable[] initializationTask = new Runnable[1];
        doAnswer(invocation -> {
            if (initializationTask[0] == null) {
                initializationTask[0] = invocation.getArgument(0);
            }
            return null;
        }).when(executor).execute(any(Runnable.class));

        storage.getOrCreate(mContext, mApiKey);

        assertThat(initializationTask[0]).isNotNull();
        initializationTask[0].run();

        verify(facadeProvider, never()).getInitializedImpl(any(Context.class));
    }

    private static void assertThreadState(Thread thread, Thread.State expectedState) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != expectedState && thread.isAlive() && System.nanoTime() < deadline) {
            Thread.yield();
        }
        assertThat(thread.getState()).isEqualTo(expectedState);
    }
}
