package io.appmetrica.analytics.impl.referrer.service.provider.huawei

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class HuaweiReferrerProviderTest : CommonTest() {
    private val context: Context = mock()
    private val result: ReferrerResult = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val huaweiReferrerContentProviderRule = constructionRule<HuaweiReferrerContentProvider> {
        on { getReferrer(context) } doReturn result
    }
    private val huaweiReferrerContentProvider by huaweiReferrerContentProviderRule

    private val huaweiReferrerProvider by setUp { HuaweiReferrerProvider(context) }

    @Test
    fun `referrerName returns huawei`() {
        assertThat(huaweiReferrerProvider.referrerName).isEqualTo("huawei")
    }

    @Test
    fun `requestReferrer creates HuaweiReferrerContentProvider and calls getReferrer`() {
        val runnerCaptor = argumentCaptor<Runnable>()
        val thread = mock<InterruptionSafeThread>()
        whenever(
            GlobalServiceLocator.getInstance().serviceExecutorProvider.getHmsReferrerThread(runnerCaptor.capture())
        ).thenReturn(thread)

        val listener: ReferrerListener = mock()
        huaweiReferrerProvider.requestReferrer(listener)

        verify(thread).start()
        verify(listener, never()).onResult(result)
        assertThat(huaweiReferrerContentProviderRule.constructionMock.constructed()).isEmpty()

        runnerCaptor.firstValue.run()

        verify(huaweiReferrerContentProvider).getReferrer(context)
        verify(listener).onResult(result)
    }
}
