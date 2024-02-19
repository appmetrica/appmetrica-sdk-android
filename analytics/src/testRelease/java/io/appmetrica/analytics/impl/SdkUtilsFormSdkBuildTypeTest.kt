package io.appmetrica.analytics.impl

import io.appmetrica.analytics.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkUtilsFormSdkBuildTypeTest {

    @Test
    fun formSdkBuildType() {
        assertThat(SdkUtils.formSdkBuildType())
            .isEqualTo("${BuildConfig.SDK_BUILD_FLAVOR}_${BuildConfig.SDK_DEPENDENCY}")
    }
}
