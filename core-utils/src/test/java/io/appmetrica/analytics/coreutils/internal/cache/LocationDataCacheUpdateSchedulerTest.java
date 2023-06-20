package io.appmetrica.analytics.coreutils.internal.cache;

import io.appmetrica.analytics.coreapi.internal.cache.UpdateConditionsChecker;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.locationapi.internal.ILastKnownUpdater;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LocationDataCacheUpdateSchedulerTest extends CommonTest {

    private static final long UPDATE_INTERVAL_SECONDS = 90;
    private final String tag = "Test tag";
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private ILastKnownUpdater mLastKnownUpdater;
    @Mock
    private UpdateConditionsChecker mUpdateConditionsChecker;
    @Captor
    private ArgumentCaptor<Runnable> mRunnableCaptor;
    private LocationDataCacheUpdateScheduler mDataCacheUpdateScheduler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mDataCacheUpdateScheduler =
            new LocationDataCacheUpdateScheduler(mExecutor, mLastKnownUpdater, mUpdateConditionsChecker, tag);
    }

    @Test
    public void testOnStateUpdated() {
        mDataCacheUpdateScheduler.onStateUpdated();
        verify(mExecutor).executeDelayed(mRunnableCaptor.capture(), eq(UPDATE_INTERVAL_SECONDS), eq(TimeUnit.SECONDS));
        mRunnableCaptor.getValue().run();
        verify(mLastKnownUpdater).updateLastKnown();
    }

    @Test
    public void testOnStateUpdatedRemovedPrevious() {
        mDataCacheUpdateScheduler.onStateUpdated();
        InOrder inOrder = Mockito.inOrder(mExecutor);
        inOrder.verify(mExecutor).remove(mRunnableCaptor.capture());
        Runnable lastRunnable = mRunnableCaptor.getValue();
        inOrder.verify(mExecutor).executeDelayed(lastRunnable, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        clearInvocations(mExecutor);
        mDataCacheUpdateScheduler.onStateUpdated();
        verify(mExecutor).remove(lastRunnable);
        verify(mExecutor).executeDelayed(lastRunnable, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    public void testStartUpdates() {
        mDataCacheUpdateScheduler.startUpdates();
        verify(mExecutor).executeDelayed(mRunnableCaptor.capture(), eq(UPDATE_INTERVAL_SECONDS), eq(TimeUnit.SECONDS));
        mRunnableCaptor.getValue().run();
        verify(mLastKnownUpdater).updateLastKnown();
    }

    @Test
    public void testStopUpdates() {
        mDataCacheUpdateScheduler.stopUpdates();
        verify(mExecutor, times(2)).remove(mRunnableCaptor.capture());
        List<Runnable> runnables = mRunnableCaptor.getAllValues();
        runnables.get(0).run();
        runnables.get(1).run();
        verify(mLastKnownUpdater).updateLastKnown();
        verify(mUpdateConditionsChecker).shouldUpdate();
    }

    @Test
    public void testUpdateNowIfNeededShouldUpdate() {
        when(mUpdateConditionsChecker.shouldUpdate()).thenReturn(true);
        mDataCacheUpdateScheduler.scheduleUpdateIfNeededNow();
        verify(mExecutor).execute(mRunnableCaptor.capture());
        mRunnableCaptor.getValue().run();
        verify(mLastKnownUpdater).updateLastKnown();
    }

    @Test
    public void testUpdateNowIfNeededShouldNotUpdate() {
        when(mUpdateConditionsChecker.shouldUpdate()).thenReturn(false);
        mDataCacheUpdateScheduler.scheduleUpdateIfNeededNow();
        verify(mExecutor).execute(mRunnableCaptor.capture());
        mRunnableCaptor.getValue().run();
        verify(mLastKnownUpdater, never()).updateLastKnown();
    }
}
