package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerManager
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class StartupRequestReferrerProviderTest : CommonTest() {

    @Test
    fun configurationReferrerWinsOverAppStoreReferrer() {
        val referrerManager = referrerManager(referrerResult("utm_source=app_store"))
        val provider = StartupRequestReferrerProvider(
            StartupRequestReferrer("utm_source=configuration", null),
            referrerManager,
        )
        val referrer = provider.referrer

        assertThat(referrer?.referrer).isEqualTo("utm_source=configuration")
        assertThat(referrer?.source).isNull()
        assertThat(provider.toString()).isEqualTo(
            "StartupRequestReferrerProvider(" +
                "referrerFromConfiguration=utm_source=configuration, " +
                "referrerSourceFromConfiguration=null, " +
                "referrerFromAppStore=null, " +
                "referrerSourceFromAppStore=null" +
                ")"
        )
        verify(referrerManager, times(0)).getCachedReferrer()
    }

    @Test
    fun appStoreReferrerIsUsedWhenConfigurationReferrerIsNullOrEmpty() {
        listOf(StartupRequestReferrer("", "api"), null).forEach { configurationReferrer ->
            val referrer = StartupRequestReferrerProvider(
                configurationReferrer,
                referrerManager(referrerResult("utm_source=app_store")),
            ).referrer

            assertThat(referrer?.referrer).isEqualTo("utm_source=app_store")
            assertThat(referrer?.source).isEqualTo("gpl")
        }
    }

    @Test
    fun appStoreReferrerIsRequestedAgainUntilNonEmptyResultIsAvailable() {
        listOf(null, referrerResult("")).forEach { unavailableReferrer ->
            val referrerManager = referrerManager(
                unavailableReferrer,
                referrerResult("utm_source=first"),
                referrerResult("utm_source=second"),
            )
            val provider = StartupRequestReferrerProvider(null, referrerManager)

            assertThat(provider.referrer?.referrer).isEqualTo(unavailableReferrer?.referrerInfo?.installReferrer)
            assertThat(provider.referrer?.referrer).isEqualTo("utm_source=first")
            assertThat(provider.referrer?.referrer).isEqualTo("utm_source=first")
            assertThat(provider.toString()).contains(
                "referrerFromAppStore=utm_source=first",
                "referrerSourceFromAppStore=gpl",
            )
            verify(referrerManager, times(2)).getCachedReferrer()
        }
    }

    @Test
    fun emptyConfigurationReferrerIsKeptWhileAppStoreReferrerIsMissing() {
        val referrer = StartupRequestReferrerProvider(
            StartupRequestReferrer("", "api"),
            referrerManager(null),
        ).referrer

        assertThat(referrer?.referrer).isEmpty()
        assertThat(referrer?.source).isEqualTo("api")
    }

    private fun referrerManager(vararg results: ReferrerResult?): ReferrerManager = mock<ReferrerManager>().also {
        whenever(it.getCachedReferrer()).thenReturn(results.first(), *results.drop(1).toTypedArray())
    }

    private fun referrerResult(referrer: String): ReferrerResult =
        ReferrerResult.Success(ReferrerInfo(referrer, 0, 0, ReferrerInfo.Source.GP))
}
