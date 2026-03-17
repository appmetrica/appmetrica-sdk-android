package io.appmetrica.analytics.impl.referrer.service.cache

import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class VitalReferrerCacheTest : CommonTest() {
    private val vitalCommonDataProvider: VitalCommonDataProvider = mock()
    private val vitalReferrerCache by setUp { VitalReferrerCache(vitalCommonDataProvider) }

    @Test
    fun `name returns vital`() {
        assertThat(vitalReferrerCache.name).isEqualTo("vital")
    }

    @Test
    fun `hasReferrer returns false when referrerChecked is false`() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)

        assertThat(vitalReferrerCache.hasReferrer()).isFalse
    }

    @Test
    fun `hasReferrer returns true when referrerChecked is true`() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(true)

        assertThat(vitalReferrerCache.hasReferrer()).isTrue
    }

    @Test
    fun `getReferrerOrNull returns null when referrer is null`() {
        whenever(vitalCommonDataProvider.referrer).thenReturn(null)

        assertThat(vitalReferrerCache.getReferrerOrNull()).isNull()
    }

    @Test
    fun `getReferrerOrNull returns Success when referrer is not null`() {
        val referrerInfo: ReferrerInfo = mock()
        whenever(vitalCommonDataProvider.referrer).thenReturn(referrerInfo)

        val result = vitalReferrerCache.getReferrerOrNull()

        assertThat(result).isInstanceOf(ReferrerResult.Success::class.java)
        assertThat(result?.referrerInfo).isEqualTo(referrerInfo)
    }

    @Test
    fun `saveReferrer with Success saves referrerInfo and sets referrerChecked`() {
        val referrerInfo: ReferrerInfo = mock()
        val successResult: ReferrerResult = mock {
            on { this.referrerInfo }.thenReturn(referrerInfo)
        }

        vitalReferrerCache.saveReferrer(successResult)

        verify(vitalCommonDataProvider).referrer = referrerInfo
        verify(vitalCommonDataProvider).referrerChecked = true
    }

    @Test
    fun `saveReferrer with Failure saves null and sets referrerChecked`() {
        val failureResult: ReferrerResult = mock {
            on { referrerInfo }.thenReturn(null)
        }

        vitalReferrerCache.saveReferrer(failureResult)

        verify(vitalCommonDataProvider).referrer = null
        verify(vitalCommonDataProvider).referrerChecked = true
    }
}
