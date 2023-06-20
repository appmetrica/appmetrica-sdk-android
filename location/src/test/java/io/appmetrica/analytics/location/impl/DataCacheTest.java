package io.appmetrica.analytics.location.impl;

import io.appmetrica.analytics.coreutils.internal.cache.DataCache;
import io.appmetrica.analytics.coreutils.internal.cache.LocationDataCacheUpdateScheduler;
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
public abstract class DataCacheTest<T> extends CommonTest {

    @Mock
    LocationDataCacheUpdateScheduler mDataCacheUpdateScheduler;

    private DataCache<T> mDataCache;
    private T mData;
    private T mAnotherData;

    private final long defaultCacheRefreshTime = TimeUnit.MINUTES.toMillis(1);
    private final long defaultCacheExpiryTime = TimeUnit.MINUTES.toMillis(2);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mDataCache = createDataCache(defaultCacheRefreshTime, defaultCacheExpiryTime);
        mData = createMockedData();
        mAnotherData = createAnotherData();
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
        long refreshTime = 10L;
        mDataCache = createDataCache(refreshTime, refreshTime + 10);
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        Thread.sleep(refreshTime + 2);
        assertThat(mDataCache.getData()).isEqualTo(mData);
        verify(mDataCacheUpdateScheduler).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testGetDataIfShouldClear() throws InterruptedException {
        long refreshTime = 10L;
        long expiryTime = 12L;
        mDataCache = createDataCache(refreshTime, expiryTime);
        mDataCache.setUpdateScheduler(mDataCacheUpdateScheduler);
        mDataCache.updateData(mData);
        Thread.sleep(expiryTime + 1);
        assertThat(mDataCache.getData()).isNull();
        verify(mDataCacheUpdateScheduler).scheduleUpdateIfNeededNow();
    }

    @Test
    public void testGetDataIfCacheExpiredAndMissedUpdater() throws InterruptedException {
        mDataCache = createDataCache(10, 20);
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
        mDataCache = createDataCache(refreshTime, 20);
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
    public void defaultRefreshTime() {
        assertThat(createDataCache().getCachedData().getRefreshTime()).isEqualTo(getDefaultCacheRefreshTime());
    }

    @Test
    public void defaultExpiryTime() {
        assertThat(createDataCache().getCachedData().getExpiryTime()).isEqualTo(getDefaultCacheExpiryTime());
    }

    @Test
    public void updateCacheControl() {
        long refreshTime = 123545;
        long expiryTime = 2312321L;
        mDataCache.updateCacheControl(refreshTime, expiryTime);
        assertThat(mDataCache.getCachedData().getRefreshTime()).isEqualTo(refreshTime);
        assertThat(mDataCache.getCachedData().getExpiryTime()).isEqualTo(expiryTime);
    }

    protected abstract DataCache<T> createDataCache(long refreshTime, long expiryTime);

    protected abstract DataCache<T> createDataCache();

    protected abstract long getDefaultCacheRefreshTime();

    protected abstract long getDefaultCacheExpiryTime();

    protected abstract T createMockedData();

    protected abstract T createAnotherData();
}
