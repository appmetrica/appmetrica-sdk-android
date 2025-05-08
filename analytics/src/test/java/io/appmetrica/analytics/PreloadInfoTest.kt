package io.appmetrica.analytics

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreloadInfoTest : CommonTest() {

    companion object {
        private const val TRACKING_ID = "test_tracking_id"
        private val ADDITIONAL_INFO = mapOf(
            "test_key_1" to "test_value_1",
            "test_key_2" to "test_value_2",
            "test_key_3" to "test_value_3"
        )
    }

    private val defaultPreloadInfoBuilder = PreloadInfo.newBuilder(TRACKING_ID)
    private val defaultPreloadInfo: PreloadInfo = defaultPreloadInfoBuilder.build()

    private lateinit var preloadInfoBuilder: PreloadInfo.Builder
    private lateinit var preloadInfo: PreloadInfo

    @Before
    fun setUp() {
        preloadInfoBuilder = defaultPreloadInfoBuilder
        preloadInfo = defaultPreloadInfo
    }

    @Test
    fun getTrackingIdReturnValueFromInitialization() {
        assertThat(preloadInfo.trackingId).isEqualTo(TRACKING_ID)
    }

    @Test
    fun getAdditionalInfoReturnValueFromInitialization() {
        for ((key, value) in ADDITIONAL_INFO) {
            preloadInfoBuilder.setAdditionalParams(key, value)
        }
        val preloadInfo = preloadInfoBuilder.build()
        for ((key, value) in preloadInfo.additionalParams) {
            assertThat(ADDITIONAL_INFO.containsKey(key)).isTrue()
            assertThat(ADDITIONAL_INFO[key]).isEqualTo(value)
        }
    }

    @Test
    fun shouldNotAddAdditionalInfoIfKeyIsNull() {
        val preloadInfo = preloadInfoBuilder.setAdditionalParams(null, "test string").build()
        assertThat(preloadInfo.additionalParams).isEmpty()
    }

    @Test
    fun shouldNotAddAdditionalInfoIfValueIsNull() {
        val preloadInfo = preloadInfoBuilder.setAdditionalParams("test string", null).build()
        assertThat(preloadInfo.additionalParams).isEmpty()
    }

    @Test
    fun getAdditionalInfoReturnEmptyMapIfNoDef() {
        assertThat(preloadInfo.additionalParams).isEmpty()
    }
}
