package io.appmetrica.analytics.impl.telephony

import android.os.Build
import android.telephony.SubscriptionInfo
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SimInfoHelperForQTest(
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

    @Mock
    private lateinit var subscriptionInfo: SubscriptionInfo

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun mobileCountryCode() {
        `when`(subscriptionInfo.mccString).thenReturn(inputValue)
        assertThat(SimInfoHelperForQ.mobileCountryCode(subscriptionInfo)).isEqualTo(expectedValue)
    }

    @Test
    fun getMobileNetworkCode() {
        `when`(subscriptionInfo.mncString).thenReturn(inputValue)
        assertThat(SimInfoHelperForQ.mobileNetworkCode(subscriptionInfo)).isEqualTo(expectedValue)
    }
}
