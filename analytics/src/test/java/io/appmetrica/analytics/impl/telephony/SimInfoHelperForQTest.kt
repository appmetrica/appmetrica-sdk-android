package io.appmetrica.analytics.impl.telephony

import android.os.Build
import android.telephony.SubscriptionInfo
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
internal class SimInfoHelperForQTest(
    private val inputValue: String?,
    private val expectedValue: Int?
) : CommonTest() {
    companion object {
        @ParameterizedRobolectricTestRunner.Parameters()
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf("123", 123),
                arrayOf(null, null),
                arrayOf("das", null)
            )
        }
    }

    private val subscriptionInfo: SubscriptionInfo = mock()

    @Test
    fun mobileCountryCode() {
        whenever(subscriptionInfo.mccString).thenReturn(inputValue)
        assertThat(SimInfoHelperForQ.mobileCountryCode(subscriptionInfo)).isEqualTo(expectedValue)
    }

    @Test
    fun getMobileNetworkCode() {
        whenever(subscriptionInfo.mncString).thenReturn(inputValue)
        assertThat(SimInfoHelperForQ.mobileNetworkCode(subscriptionInfo)).isEqualTo(expectedValue)
    }
}
