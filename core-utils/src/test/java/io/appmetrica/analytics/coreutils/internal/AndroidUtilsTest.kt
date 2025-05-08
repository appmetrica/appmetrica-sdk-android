package io.appmetrica.analytics.coreutils.internal

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidUtilsTest : CommonTest() {

    private val apiLevel = Build.VERSION_CODES.P

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isApiAchievedForSame() {
        assertThat(AndroidUtils.isApiAchieved(apiLevel)).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isApiAchievedForLower() {
        assertThat(AndroidUtils.isApiAchieved(apiLevel)).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun isApiAchievedForHigher() {
        assertThat(AndroidUtils.isApiAchieved(apiLevel)).isFalse()
    }
}
