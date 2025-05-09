package io.appmetrica.analytics.coreutils.internal.network

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserAgentTest : CommonTest() {

    @Test
    fun formSdkUserAgent() {
        val sdkName = "my sdk name"
        val versionName = "versionName"
        val buildNumber = "buildNumber"
        assertThat(UserAgent.getFor(sdkName, versionName, buildNumber))
            .contains("$sdkName/$versionName.$buildNumber", "; Android ")
    }
}
