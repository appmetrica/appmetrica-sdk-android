package io.appmetrica.analytics.testutils

import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter
import io.appmetrica.analytics.coreapi.internal.system.LocaleProvider
import io.appmetrica.analytics.networktasks.internal.AppInfo
import io.appmetrica.analytics.networktasks.internal.NetworkAppContext
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator
import io.appmetrica.analytics.networktasks.internal.ScreenInfoProvider
import io.appmetrica.analytics.networktasks.internal.SdkInfo
import org.junit.rules.ExternalResource
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Copy of appmetrica-sdk/analytics/src/test/java/io/appmetrica/analycits/testutils/rules/networktasks/NetworkServiceLocatorRule.kt
 * https://nda.ya.ru/t/nY86Qzos6Njj7W
 */
class NetworkServiceLocatorRule : ExternalResource() {

    lateinit var mockedStatic: MockedStatic<NetworkServiceLocator>

    override fun before() {
        super.before()
        mockedStatic = Mockito.mockStatic(NetworkServiceLocator::class.java)
        val appInfoMock = mock<AppInfo> {
            on { appFramework } doReturn "app_framework"
        }
        val sdkInfoMock = mock<SdkInfo> {
            on { sdkVersionName } doReturn "5.0.0"
            on { sdkBuildNumber } doReturn "500000000"
            on { sdkBuildType } doReturn "internalProdRelease"
        }
        val screenInfoMock = mock<ScreenInfo> {
            on { width } doReturn 600
            on { height } doReturn 800
            on { dpi } doReturn 160
            on { scaleFactor } doReturn 2f
            on { deviceType } doReturn DeviceTypeValues.TABLET
        }

        val screenInfoProviderMock = mock<ScreenInfoProvider> {
            on { screenInfo } doReturn screenInfoMock
        }

        val advertisingIdsHolder = prepareAdvertisingIdHolder()

        val advertisingIdGetterMock = mock<SimpleAdvertisingIdGetter> {
            on { getIdentifiers(any()) } doReturn advertisingIdsHolder
        }

        val localeProviderMock = mock<LocaleProvider> {
            on { locales } doReturn listOf("ru", "en")
        }

        val appSetIdMock = AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER)

        val appSetIdProviderMock = mock<AppSetIdProvider> {
            on { getAppSetId() } doReturn appSetIdMock
        }

        val networkAppContextMock = mock<NetworkAppContext> {
            on { appInfo } doReturn appInfoMock
            on { sdkInfo } doReturn sdkInfoMock
            on { screenInfoProvider } doReturn screenInfoProviderMock
            on { advertisingIdGetter } doReturn advertisingIdGetterMock
            on { localeProvider } doReturn localeProviderMock
            on { appSetIdProvider } doReturn appSetIdProviderMock
        }

        val networkServiceMock = mock<NetworkServiceLocator> {
            on { networkCore } doReturn mock()
            on { networkAppContext } doReturn networkAppContextMock
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

