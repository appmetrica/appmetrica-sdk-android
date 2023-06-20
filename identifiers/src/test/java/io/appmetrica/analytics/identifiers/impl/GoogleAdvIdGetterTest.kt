package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoogleAdvIdGetterTest {

    @get:Rule
    internal val staticMock = MockedStaticRule(AdvertisingIdClient::class.java)

    private val context = mock<Context>()

    @Test
    fun fineCase() {
        val someId = "someId"
        val limited = false
        val info = mock<AdvertisingIdClient.Info> {
            on { id }.thenReturn(someId)
            on { isLimitAdTrackingEnabled }.thenReturn(limited)
        }
        stubbing(staticMock.staticMock) {
            on { AdvertisingIdClient.getAdvertisingIdInfo(context) } doReturn info
        }
        assertThat(GoogleAdvIdGetter().getAdTrackingInfo(context)).isEqualTo(
            AdsIdResult(
                IdentifierStatus.OK,
                AdsIdInfo(
                    Constants.Providers.GOOGLE,
                    someId, limited
                )
            )
        )
    }

    @Test
    fun libraryNotAvailable() {
        stubbing(staticMock.staticMock) {
            on {
                AdvertisingIdClient.getAdvertisingIdInfo(context)
            } doThrow GooglePlayServicesNotAvailableException(100500)
        }
        assertThat(GoogleAdvIdGetter().getAdTrackingInfo(context)).isEqualTo(
            AdsIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                errorExplanation = "could not resolve google services"
            )
        )
    }

    @Test
    fun genericError() {
        val message = "some error message"
        stubbing(staticMock.staticMock) {
            on { AdvertisingIdClient.getAdvertisingIdInfo(context) } doThrow RuntimeException(message)
        }
        assertThat(GoogleAdvIdGetter().getAdTrackingInfo(context)).isEqualTo(
            AdsIdResult(
                IdentifierStatus.UNKNOWN,
                errorExplanation = "exception while fetching google adv_id: $message"
            )
        )
    }
}
