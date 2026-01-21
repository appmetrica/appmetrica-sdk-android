package io.appmetrica.analytics.location.impl

import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.coreutils.internal.cache.DataCache
import io.appmetrica.analytics.coreutils.internal.cache.LocationDataCacheUpdateScheduler
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
abstract class DataCacheTest<T> : CommonTest() {

    protected val dataCacheUpdateScheduler: LocationDataCacheUpdateScheduler = mock()

    protected lateinit var dataCache: DataCache<T>
    protected var data: T? = null
    protected var anotherData: T? = null

    private val defaultCacheRefreshTime = TimeUnit.MINUTES.toMillis(1)
    private val defaultCacheExpiryTime = TimeUnit.MINUTES.toMillis(2)

    @get:Rule
    val timeProviderRule = MockedConstructionRule(SystemTimeProvider::class.java)

    @get:Rule
    val cachedDataRule = MockedConstructionRule(CachedDataProvider.CachedData::class.java)

    @Before
    fun setUp() {
        dataCache = createDataCache(defaultCacheRefreshTime, defaultCacheExpiryTime)
        data = createMockedData()
        anotherData = createAnotherData()
    }

    protected fun cachedData(): CachedDataProvider.CachedData<T> {
        assertThat(cachedDataRule.constructionMock.constructed()).isNotEmpty
        val constructed = cachedDataRule.constructionMock.constructed()
        @Suppress("UNCHECKED_CAST")
        return constructed[constructed.size - 1] as CachedDataProvider.CachedData<T>
    }

    @Test
    fun updateData() {
        dataCache.updateData(data!!)

        verify(cachedData()).setData(data!!)
    }

    @Test
    fun updateDataNullScheduler() {
        dataCache.updateData(data!!)

        verify(cachedData()).setData(data!!)
    }

    @Test
    fun updateDataTriggersScheduler() {
        dataCache.setUpdateScheduler(dataCacheUpdateScheduler)
        dataCache.updateData(data!!)

        verify(cachedData()).setData(data!!)
        verify(dataCacheUpdateScheduler).onStateUpdated()
    }

    @Test
    fun getData() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().shouldClearData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(data!!)

        dataCache.setUpdateScheduler(dataCacheUpdateScheduler)
        dataCache.updateData(data!!)

        assertThat(dataCache.data).isEqualTo(data!!)
        verify(dataCacheUpdateScheduler, never()).scheduleUpdateIfNeededNow()
    }

    @Test
    fun getDataIfShouldUpdate() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(true)
        whenever(cachedData().shouldClearData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(data!!)

        dataCache.setUpdateScheduler(dataCacheUpdateScheduler)
        dataCache.updateData(data!!)

        assertThat(dataCache.data).isEqualTo(data!!)
        verify(dataCacheUpdateScheduler).scheduleUpdateIfNeededNow()
    }

    @Test
    fun getDataIfShouldClear() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().shouldClearData()).thenReturn(true)
        whenever(cachedData().data).thenReturn(null)

        dataCache.setUpdateScheduler(dataCacheUpdateScheduler)
        dataCache.updateData(data!!)

        assertThat(dataCache.data).isNull()
        verify(cachedData()).setData(null)
    }

    @Test
    fun getDataIfCacheExpiredAndMissedUpdater() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(true)
        whenever(cachedData().shouldClearData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(data!!)

        dataCache.updateData(data!!)
        dataCache.data

        // No scheduler, so scheduleUpdateIfNeededNow should not be called
    }

    @Test
    fun getDataIfCacheIsEmpty() {
        whenever(cachedData().isEmpty).thenReturn(true)
        whenever(cachedData().shouldClearData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(null)

        dataCache.setUpdateScheduler(dataCacheUpdateScheduler)
        dataCache.data

        verify(dataCacheUpdateScheduler).scheduleUpdateIfNeededNow()
    }

    @Test
    fun shouldUpdateIfCacheIsEmpty() {
        whenever(cachedData().isEmpty).thenReturn(true)

        assertThat(dataCache.shouldUpdate()).isTrue
    }

    @Test
    fun shouldUpdateCacheIsOutdated() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(true)

        dataCache.updateData(data!!)

        assertThat(dataCache.shouldUpdate()).isTrue
    }

    @Test
    fun shouldNotUpdate() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)

        dataCache.updateData(data!!)

        assertThat(dataCache.shouldUpdate()).isFalse
    }

    @Test
    fun updateCacheControl() {
        val refreshTime = 123545L
        val expiryTime = 2312321L

        dataCache.updateCacheControl(refreshTime, expiryTime)

        verify(cachedData()).setExpirationPolicy(refreshTime, expiryTime)
    }

    @Test
    fun shouldUpdateWhenTimeGoesBackwards() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(true)

        dataCache.updateData(data!!)

        assertThat(dataCache.shouldUpdate()).isTrue
    }

    @Test
    fun shouldClearWhenTimeGoesBackwards() {
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().shouldClearData()).thenReturn(true)
        whenever(cachedData().data).thenReturn(null)

        dataCache.updateData(data!!)

        assertThat(dataCache.data).isNull()
        verify(cachedData()).setData(null)
    }

    protected abstract fun createDataCache(refreshTime: Long, expiryTime: Long): DataCache<T>

    protected abstract fun createMockedData(): T

    protected abstract fun createAnotherData(): T
}
