package io.appmetrica.analytics.snapshot.impl

import android.text.TextUtils
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer

internal class SdkUtilsFormSdkBuildTypeTest : CommonTest() {

    @get:Rule
    val textUtils = staticRule<TextUtils> {
        on { TextUtils.isEmpty(any()) } doAnswer { invocation ->
            (invocation.arguments[0] as CharSequence?).isNullOrEmpty()
        }
    }

    @Test
    fun formSdkBuildType() {
        assertThat(SdkUtils.formSdkBuildType())
            .isEqualTo(
                "${BuildConfig.SDK_BUILD_FLAVOR}_${BuildConfig.SDK_DEPENDENCY}_${BuildConfig.SDK_BUILD_TYPE}"
            )
    }
}
