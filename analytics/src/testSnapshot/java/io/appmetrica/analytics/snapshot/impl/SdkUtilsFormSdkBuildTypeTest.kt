package io.appmetrica.analytics.snapshot.impl

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkUtilsFormSdkBuildTypeTest : CommonTest() {

    @Test
    fun formSdkBuildType() {
        Assertions.assertThat(SdkUtils.formSdkBuildType())
            .isEqualTo("${BuildConfig.SDK_BUILD_FLAVOR}_${BuildConfig.SDK_DEPENDENCY}_${BuildConfig.SDK_BUILD_TYPE}")
    }
}
