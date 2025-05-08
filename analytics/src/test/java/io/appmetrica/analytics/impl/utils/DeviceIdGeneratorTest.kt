package io.appmetrica.analytics.impl.utils

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter
import io.appmetrica.analytics.impl.id.AppSetIdGetter
import io.appmetrica.analytics.impl.id.Constants
import io.appmetrica.analytics.impl.id.RetryStrategy
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeviceIdGeneratorTest : CommonTest() {

    private val context = mock<Context>()
    private val advertisingIdsHolder = mock<AdvertisingIdsHolder>()
    private val advertisingIdGetter = mock<AdvertisingIdGetter>()
    private val appSetIdGetter = mock<AppSetIdGetter>()
    private val deviceIdGenerator = DeviceIdGenerator(context, advertisingIdGetter, appSetIdGetter)
    private val identifierPatter = "[0-9a-f]{32}"

    @Test
    fun correctStrategyIsUsed() {
        val captor = argumentCaptor<RetryStrategy>()
        stubbing(advertisingIdGetter) {
            on { getIdentifiersForced(captor.capture()) } doReturn advertisingIdsHolder
        }
        stubbing(advertisingIdsHolder) {
            on { yandex } doReturn AdTrackingInfoResult(
                AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, "id", false),
                IdentifierStatus.OK,
                null
            )
        }
        deviceIdGenerator.generateDeviceId()
        assertThat(captor.firstValue).isInstanceOf(TimesBasedRetryStrategy::class.java)
        ObjectPropertyAssertions(captor.firstValue as TimesBasedRetryStrategy)
            .withPrivateFields(true)
            .checkField("maxAttempts", 5)
            .checkField("timeout", 500)
            .checkAll()
    }

    @Test
    fun validYandexAdvId() {
        val yandexAdvId = "yandex adv id"
        stubbing(advertisingIdGetter) {
            on { getIdentifiersForced(any()) } doReturn advertisingIdsHolder
        }
        stubbing(advertisingIdsHolder) {
            on { yandex } doReturn AdTrackingInfoResult(
                AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                IdentifierStatus.OK,
                null
            )
        }
        assertThat(deviceIdGenerator.generateDeviceId())
            .isEqualTo(StringUtils.toHexString(IOUtils.md5(yandexAdvId.toByteArray())))
        verifyNoInteractions(appSetIdGetter)
    }

    @Test
    fun invalidYandexAdvIdHasAppSetId() {
        mockInvalidYandexAdvId()
        val appSetId = "85811c52-16ca-42c4-867e-1286421a0367"
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(appSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).isEqualTo("85811c5216ca42c4867e1286421a0367")
    }

    @Test
    fun invalidYandexAdvIdHasNullSetId() {
        mockInvalidYandexAdvId()
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(null, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    @Test
    fun invalidYandexAdvIdHasEmptyAppSetId() {
        mockInvalidYandexAdvId()
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId("", AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    @Test
    fun invalidYandexAdvIdHasZeroAppSetId() {
        mockInvalidYandexAdvId()
        val invalidAppSetId = "00000"
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(invalidAppSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter).isNotEqualTo(invalidAppSetId)
    }

    @Test
    fun invalidYandexAdvIdHasZeroWithDashesAppSetId() {
        mockInvalidYandexAdvId()
        val invalidAppSetId = "000-00"
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(invalidAppSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    @Test
    fun invalidYandexAdvIdHasZeroWithCorrectPatternAppSetId() {
        mockInvalidYandexAdvId()
        val invalidAppSetId = Constants.INVALID_ADV_ID
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(invalidAppSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    @Test
    fun invalidYandexAdvIdHasZeroWithInvalidAdvIdAppSetIdWithoutDashes() {
        mockInvalidYandexAdvId()
        val invalidAppSetId = "00000000000000000000000000000000"
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(invalidAppSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    @Test
    fun invalidYandexAdvIdHasInvalidUuidAppSetId() {
        mockInvalidYandexAdvId()
        val invalidAppSetId = "aaabbb"
        stubbing(appSetIdGetter) {
            on { getAppSetId() } doReturn AppSetId(invalidAppSetId, AppSetIdScope.APP)
        }
        assertThat(deviceIdGenerator.generateDeviceId()).matches(identifierPatter)
    }

    private fun mockInvalidYandexAdvId() {
        stubbing(advertisingIdGetter) {
            on { getIdentifiersForced(any()) } doReturn advertisingIdsHolder
        }
        stubbing(advertisingIdsHolder) {
            on { yandex } doReturn AdTrackingInfoResult(
                AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, null, false),
                IdentifierStatus.OK,
                null
            )
        }
    }
}
