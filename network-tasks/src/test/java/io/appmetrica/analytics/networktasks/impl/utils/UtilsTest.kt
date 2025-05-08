package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.networktasks.internal.utils.Utils
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilsTest : CommonTest() {

    @Test
    fun isBadRequest() {
        val softly = SoftAssertions()
        softly.assertThat(Utils.isBadRequest(-400)).`as`("code -400").isFalse
        (-10..399).forEach {
            softly.assertThat(Utils.isBadRequest(it)).`as`("code $it").isFalse
        }
        softly.assertThat(Utils.isBadRequest(400)).`as`("code 400").isTrue
        (401..999).forEach {
            softly.assertThat(Utils.isBadRequest(it)).`as`("code $it").isFalse
        }
        softly.assertAll()
    }
}
