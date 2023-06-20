package io.appmetrica.analytics.coreutils.internal.cache;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DataCacheTest extends CommonTest {

    class TestDataCache extends DataCache<Object> {

        public TestDataCache(long refreshTime, long expiryTime, @NonNull String description) {
            super(refreshTime, expiryTime, description);
        }

        @Override
        protected boolean shouldUpdate(@NonNull Object newData) {
            return true;
        }
    }

    private final String description = "Some cache description";

    @Mock
    LocationDataCacheUpdateScheduler mDataCacheUpdateScheduler;

    private DataCache<Object> mDataCache;
    @Mock
    private Object mData;

    private final long defaultCacheRefreshTime = TimeUnit.MINUTES.toMillis(1);
    private final long defaultCacheExpiryTime = TimeUnit.MINUTES.toMillis(2);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mDataCache = new TestDataCache(defaultCacheRefreshTime, defaultCacheExpiryTime, description);
    }

    @Test
    public void testUpdateData() {
        mDataCache.updateData(mData);
        assertThat(mDataCache.getData()).isEqualTo(mData);
    }

    @Test
    public void testUpdateDataNullScheduler() {
        mDataCache.updateData(mData);
    }

    @Test
    public void testUpdateDataTriggersScheduler() {
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        verify(mDataCacheUpdateScheduler).onStateUpdated();
    }

    @Test
    public void testGetData() {
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        assertThat(mDataCache.getData()).isEqualTo(mData);
        verify(mDataCacheUpdateScheduler, never()).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testGetDataIfShouldUpdate() throws InterruptedException {
        long refreshTime = 1000L;
        long invalidateDelta = 1000L;
        long accuracy = 100L;
        mDataCache = new TestDataCache(refreshTime, refreshTime + invalidateDelta, description);
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        Thread.sleep(refreshTime + accuracy);
        assertThat(mDataCache.getData()).isEqualTo(mData);
        verify(mDataCacheUpdateScheduler).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testGetDataIfShouldClear() throws InterruptedException {
        long refreshTime = 10L;
        long expiryTime = 12L;
        mDataCache = new TestDataCache(refreshTime, expiryTime, description);
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        Thread.sleep(expiryTime + 1);
        assertThat(mDataCache.getData()).isNull();
        verify(mDataCacheUpdateScheduler).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testGetDataIfCacheExpiredAndMissedUpdater() throws InterruptedException {
        mDataCache = new TestDataCache(10, 20, description);
        mDataCache.updateData(mData);
        Thread.sleep(10);
        mDataCache.getData();
    }

    @Test
    public void testGetDataIfCacheIsEmpty() {
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.getData();
        verify(mDataCacheUpdateScheduler).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testShouldUpdateIfCacheIsEmpty() {
        assertThat(mDataCache.shouldUpdate()).isTrue();
    }

    @Test
    public void testShouldUpdateCacheIsOutdated() {
        long refreshTime = 10L;
        mDataCache = new TestDataCache(refreshTime, 20, description);
        mDataCache.updateData(mData);
        try {
            Thread.sleep(refreshTime + 1);
        } catch (InterruptedException e) {}
        assertThat(mDataCache.shouldUpdate()).isTrue();
    }

    @Test
    public void testShouldNotUpdate() {
        mDataCache.updateData(mData);
        assertThat(mDataCache.shouldUpdate()).isFalse();
    }

    @Test
    public void updateCacheControl() {
        long refreshTime = 123545;
        long expiryTime = 2312321L;
        mDataCache.updateCacheControl(refreshTime, expiryTime);
        assertThat(mDataCache.getCachedData().getRefreshTime()).isEqualTo(refreshTime);
        assertThat(mDataCache.getCachedData().getExpiryTime()).isEqualTo(expiryTime);
    }
}
