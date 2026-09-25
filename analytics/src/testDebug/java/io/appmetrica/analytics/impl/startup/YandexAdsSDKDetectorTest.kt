package io.appmetrica.analytics.impl.startup

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class YandexAdsSDKDetectorTest : CommonTest() {

    @Test
    fun isPresentCachesResultAndReportsAbsence() {
        var calls = 0
        var lastCheckedClass: String? = null
        val present = YandexAdsSDKDetector { className ->
            calls++
            lastCheckedClass = className
            true
        }
        assertThat(present.isPresent).isTrue
        assertThat(present.isPresent).isTrue
        assertThat(calls).isEqualTo(1)
        assertThat(lastCheckedClass).isEqualTo(YandexAdsSDKDetector.MARKER_CLASS_NAME)

        assertThat(YandexAdsSDKDetector { false }.isPresent).isFalse
    }
}
