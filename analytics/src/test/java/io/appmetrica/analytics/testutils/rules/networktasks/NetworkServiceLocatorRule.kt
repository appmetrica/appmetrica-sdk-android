package io.appmetrica.analytics.testutils.rules.networktasks

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator
import org.junit.rules.ExternalResource
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Copy of appmetrica-sdk/network-tasks/src/test/java/io/appmetrica/analycits/testutils/NetworkServiceLocatorRule.kt
 * https://nda.ya.ru/t/nY86Qzos6Njj7W
 */
class NetworkServiceLocatorRule : ExternalResource() {

    lateinit var mockedStatic: MockedStatic<NetworkServiceLocator>

    override fun before() {
        super.before()
        mockedStatic = Mockito.mockStatic(NetworkServiceLocator::class.java)

        val appSetIdMock = AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER)

        val appSetIdProviderMock = mock<AppSetIdProvider> {
            on { getAppSetId() } doReturn appSetIdMock
        }

        val networkServiceMock = mock<NetworkServiceLocator> {
            on { networkCore } doReturn mock()
        }
        whenever(NetworkServiceLocator.getInstance()).thenReturn(networkServiceMock)
    }

    private fun prepareAdvertisingIdHolder(): AdvertisingIdsHolder {
        val googleTrackingInfo = AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, UUID.randomUUID().toString(), false)
        val huaweiTrackingInfo = AdTrackingInfo(AdTrackingInfo.Provider.HMS, UUID.randomUUID().toString(), false)
        val yandexTrackingInfo = AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, UUID.randomUUID().toString(), true)

        val googleResult = AdTrackingInfoResult(googleTrackingInfo, IdentifierStatus.OK, null)
        val huaweiResult = AdTrackingInfoResult(huaweiTrackingInfo, IdentifierStatus.OK, null)
        val yandexResult = AdTrackingInfoResult(yandexTrackingInfo, IdentifierStatus.OK, null)

        val advertisingIdsHolder = mock<AdvertisingIdsHolder> {
            on { google } doReturn googleResult
            on { huawei } doReturn huaweiResult
            on { yandex } doReturn yandexResult
        }

        return advertisingIdsHolder
    }

    override fun after() {
        super.after()
        mockedStatic.close()
    }
}
