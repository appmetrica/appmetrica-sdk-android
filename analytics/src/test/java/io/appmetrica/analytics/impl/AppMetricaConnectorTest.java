package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaConnectorTest extends CommonTest {

    private Context mContext;
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;

    private AppMetricaConnector mConnector;

    @Rule
    public final MockedStaticRule<ScreenInfoRetriever> sScreenInfoRetriever =
        new MockedStaticRule<>(ScreenInfoRetriever.class);

    @Rule
    public final MockedConstructionRule<CountDownLatch> countDownLatchMockedConstructionRule =
        new MockedConstructionRule<>(CountDownLatch.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(ScreenInfoRetriever.getInstance(any(Context.class))).thenReturn(mock(ScreenInfoRetriever.class));
        mConnector = new AppMetricaConnector(mContext, mExecutor, appMetricaServiceDelayHandler);
    }

    @Test
    public void testForbidDisconnect() {
        mConnector.forbidDisconnect();
        Runnable unbindRunnable = mConnector.getUnbindRunnable();
        verify(mExecutor).remove(same(unbindRunnable));
        verify(mExecutor, never()).executeDelayed(same(unbindRunnable), anyLong());
    }

    @Test
    public void testAllowDisconnect() {
        mConnector.allowDisconnect();
        Runnable unbindRunnable = mConnector.getUnbindRunnable();
        verify(mExecutor).remove(same(unbindRunnable));
        verify(mExecutor).executeDelayed(same(unbindRunnable), anyLong());
    }

    @Test
    public void testScheduleDisconnectDefault() {
        testScheduleDisconnectAllowedInternal(1);
    }

    @Test
    public void testScheduleDisconnectAllowed() {
        mConnector.allowDisconnect();
        testScheduleDisconnectAllowedInternal(2);
    }

    @Test
    public void testScheduleDisconnectForbidden() {
        Runnable unbindRunnable = mConnector.getUnbindRunnable();
        mConnector.forbidDisconnect();
        mConnector.scheduleDisconnect();
        verify(mExecutor, times(2)).remove(same(unbindRunnable));
        verify(mExecutor, never()).executeDelayed(same(unbindRunnable), anyLong());
    }

    @Test
    public void bindServiceCallsDelay() {
        mConnector.bindService();
        verify(appMetricaServiceDelayHandler).maybeDelay(mContext);
    }

    @Test
    public void waitForConnectBeforeBind() {
        assertThat(mConnector.waitForConnect(100500L)).isFalse();
        assertThat(countDownLatchMockedConstructionRule.getConstructionMock().constructed()).isEmpty();
    }

    @Test
    public void waitForConnectAfterBind() throws InterruptedException {
        mConnector.bindService();
        assertThat(countDownLatchMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(countDownLatchMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(1);
        CountDownLatch countDownLatch = countDownLatchMockedConstructionRule.getConstructionMock().constructed().get(0);

        long waitTime = 100500;
        when(countDownLatch.await(waitTime, TimeUnit.MILLISECONDS)).thenReturn(true);

        assertThat(mConnector.waitForConnect(waitTime)).isTrue();
        verify(countDownLatch).await(waitTime, TimeUnit.MILLISECONDS);
    }

    @Test
    public void bindToServiceReCreateCountDownLatch() throws InterruptedException {
        mConnector.bindService();
        mConnector.bindService();
        assertThat(countDownLatchMockedConstructionRule.getConstructionMock().constructed()).hasSize(2);

        long waitTime = 200500L;
        mConnector.waitForConnect(waitTime);

        verifyNoInteractions(countDownLatchMockedConstructionRule.getConstructionMock().constructed().get(0));
        verify(countDownLatchMockedConstructionRule.getConstructionMock().constructed().get(1))
            .await(waitTime, TimeUnit.MILLISECONDS);
    }

    private void testScheduleDisconnectAllowedInternal(int attemptsToRemoveRunnable) {
        Runnable unbindRunnable = mConnector.getUnbindRunnable();
        mConnector.scheduleDisconnect();
        verify(mExecutor, times(attemptsToRemoveRunnable)).remove(same(unbindRunnable));
        verify(mExecutor, times(attemptsToRemoveRunnable)).executeDelayed(same(unbindRunnable), anyLong());
    }

}
