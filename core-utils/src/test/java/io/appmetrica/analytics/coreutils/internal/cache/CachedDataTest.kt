package io.appmetrica.analytics.coreutils.internal.cache

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class CachedDataTest : CommonTest() {

    private lateinit var cachedData: CachedDataProvider.CachedData<String>

    private val refreshTime = 1000L
    private val expiryTime = 2000L
    private val description = "test-cache"

    @get:Rule
    val timeProviderRule = MockedConstructionRule(SystemTimeProvider::class.java)

    @Before
    fun setUp() {
        cachedData = CachedDataProvider.CachedData(refreshTime, expiryTime, description)
    }

    private fun systemTimeProvider(): SystemTimeProvider {
        assertThat(timeProviderRule.constructionMock.constructed()).isNotEmpty
        assertThat(timeProviderRule.argumentInterceptor.flatArguments()).isEmpty()
        // Return the LAST constructed instance (tests may create multiple objects)
        val constructed = timeProviderRule.constructionMock.constructed()
        return constructed[constructed.size - 1]
    }

    @Test
    fun `isEmpty returns true when no data set`() {
        assertThat(cachedData.isEmpty).isTrue()
    }

    @Test
    fun `isEmpty returns false when data is set`() {
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(1000L)
        cachedData.setData("test")
        assertThat(cachedData.isEmpty).isFalse()
    }

    @Test
    fun `getData returns null initially`() {
        assertThat(cachedData.data).isNull()
    }

    @Test
    fun `getData returns set data`() {
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(1000L)
        cachedData.setData("test-value")
        assertThat(cachedData.data).isEqualTo("test-value")
    }

    @Test
    fun `setData updates cached time`() {
        val currentTime = 5000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            currentTime,
            currentTime + refreshTime / 2
        )

        cachedData.setData("data")

        // Verify time was recorded by checking shouldUpdateData
        assertThat(cachedData.shouldUpdateData()).isFalse()
    }

    @Test
    fun `shouldUpdateData returns false within refresh time`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + refreshTime / 2
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isFalse()
    }

    @Test
    fun `shouldUpdateData returns false exactly at refresh time boundary`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + refreshTime
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isFalse()
    }

    @Test
    fun `shouldUpdateData returns true just past refresh time`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + refreshTime + 1
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isTrue()
    }

    @Test
    fun `shouldUpdateData returns true when time goes backwards`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(startTime, startTime - 100)

        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isTrue()
    }

    @Test
    fun `shouldClearData returns false when no data set`() {
        assertThat(cachedData.shouldClearData()).isFalse()
    }

    @Test
    fun `shouldClearData returns false within expiry time`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + expiryTime / 2
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldClearData()).isFalse()
    }

    @Test
    fun `shouldClearData returns false exactly at expiry time boundary`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + expiryTime
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldClearData()).isFalse()
    }

    @Test
    fun `shouldClearData returns true just past expiry time`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + expiryTime + 1
        )

        cachedData.setData("data")

        assertThat(cachedData.shouldClearData()).isTrue()
    }

    @Test
    fun `shouldClearData returns true when time goes backwards`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(startTime, startTime - 100)

        cachedData.setData("data")

        assertThat(cachedData.shouldClearData()).isTrue()
    }

    @Test
    fun `setExpirationPolicy updates refresh time`() {
        val newRefreshTime = 5000L
        val newExpiryTime = 10000L

        cachedData.setExpirationPolicy(newRefreshTime, newExpiryTime)

        assertThat(cachedData.refreshTime).isEqualTo(newRefreshTime)
        assertThat(cachedData.expiryTime).isEqualTo(newExpiryTime)
    }

    @Test
    fun `setExpirationPolicy affects shouldUpdateData`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime,
            startTime + 1500,
            startTime + 1500
        )

        cachedData.setData("data")

        // With original refreshTime (1000ms), should update
        assertThat(cachedData.shouldUpdateData()).isTrue()

        // After setting longer refreshTime (2000ms), should not update
        cachedData.setExpirationPolicy(2000, expiryTime)
        assertThat(cachedData.shouldUpdateData()).isFalse()
    }

    @Test
    fun `setData to null makes cache empty`() {
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(1000L, 2000L)

        cachedData.setData("data")
        assertThat(cachedData.isEmpty).isFalse()

        cachedData.setData(null)
        assertThat(cachedData.isEmpty).isTrue()
    }

    @Test
    fun `multiple setData updates cached time each time`() {
        val startTime = 10000L
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(
            startTime, // First setData
            startTime + 500, // Second setData
            startTime + 600 // Check shouldUpdateData
        )

        cachedData.setData("data1")
        cachedData.setData("data2")

        // Time since second setData is only 100ms, within refresh window
        assertThat(cachedData.shouldUpdateData()).isFalse()
    }

    @Test
    fun `refresh time of zero means always should update`() {
        timeProviderRule.resetMock()

        val startTime = 10000L
        cachedData = CachedDataProvider.CachedData(0, expiryTime, description)

        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(startTime, startTime + 1)
        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isTrue()
    }

    @Test
    fun `expiry time of zero means data clears immediately`() {
        timeProviderRule.resetMock()

        val startTime = 10000L
        cachedData = CachedDataProvider.CachedData(refreshTime, 0, description)

        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(startTime, startTime + 1)
        cachedData.setData("data")

        assertThat(cachedData.shouldClearData()).isTrue()
    }

    @Test
    fun `very large time values do not overflow`() {
        timeProviderRule.resetMock()

        cachedData =
            CachedDataProvider.CachedData(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2, description)

        val startTime = Long.MAX_VALUE / 2
        whenever(systemTimeProvider().currentTimeMillis()).thenReturn(startTime, startTime + 1000)

        cachedData.setData("data")

        assertThat(cachedData.shouldUpdateData()).isFalse()
        assertThat(cachedData.shouldClearData()).isFalse()
    }

    @Test
    fun `setData can store complex objects`() {
        data class ComplexData(val id: Int, val name: String)

        timeProviderRule.resetMock()
        val complexCachedData = CachedDataProvider.CachedData<ComplexData>(
            refreshTime, expiryTime, description
        )
        whenever(timeProviderRule.constructionMock.constructed()[0].currentTimeMillis()).thenReturn(
            1000L
        )

        val testData = ComplexData(42, "test")
        complexCachedData.setData(testData)

        assertThat(complexCachedData.data).isEqualTo(testData)
        assertThat(complexCachedData.data?.id).isEqualTo(42)
        assertThat(complexCachedData.data?.name).isEqualTo("test")
    }
}
