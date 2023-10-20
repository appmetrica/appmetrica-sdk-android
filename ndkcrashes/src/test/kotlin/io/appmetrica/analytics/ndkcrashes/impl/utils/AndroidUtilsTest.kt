package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidUtilsTest : CommonTest() {

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun isAndroidMAchievedForSame() {
        assertThat(AndroidUtils.isAndroidMAchieved()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun isAndroidMAchievedForLower() {
        assertThat(AndroidUtils.isAndroidMAchieved()).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun isAndroidMAchievedForHigher() {
        assertThat(AndroidUtils.isAndroidMAchieved()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun isAndroidNAchievedForSame() {
        assertThat(AndroidUtils.isAndroidNAchieved()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun isAndroidNAchievedForLower() {
        assertThat(AndroidUtils.isAndroidNAchieved()).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun isAndroidNAchievedForHigher() {
        assertThat(AndroidUtils.isAndroidNAchieved()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isAndroidQAchievedForSame() {
        assertThat(AndroidUtils.isAndroidQAchieved()).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isAndroidQAchievedForLower() {
        assertThat(AndroidUtils.isAndroidQAchieved()).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun isAndroidQAchievedForHigher() {
        assertThat(AndroidUtils.isAndroidQAchieved()).isTrue()
    }
}
