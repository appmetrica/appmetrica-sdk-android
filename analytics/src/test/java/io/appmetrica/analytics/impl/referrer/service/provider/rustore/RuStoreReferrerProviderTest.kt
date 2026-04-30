package io.appmetrica.analytics.impl.referrer.service.provider.rustore

import android.content.Context
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class RuStoreReferrerProviderTest : CommonTest() {

    private val context: Context = mock()

    @get:Rule
    val ruStoreReferrerServiceRule = constructionRule<RuStoreReferrerService>()
    private val ruStoreReferrerService by ruStoreReferrerServiceRule

    private val provider by setUp { RuStoreReferrerProvider(context) }

    @Test
    fun `referrerName is rustore`() {
        assertThat(provider.referrerName).isEqualTo("rustore")
    }

    @Test
    fun `requestReferrer creates RuStoreReferrerService with context and calls requestReferrer`() {
        val listener: ReferrerListener = mock()
        provider.requestReferrer(listener)

        assertThat(ruStoreReferrerServiceRule.constructionMock.constructed()).hasSize(1)
        assertThat(ruStoreReferrerServiceRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        verify(ruStoreReferrerService).requestReferrer(listener)
    }
}
