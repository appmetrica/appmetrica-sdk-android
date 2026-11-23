package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BillingClientStateListenerImplTest : CommonTest() {

    private val billingClient: BillingClient = mock()
    private val holder: BillingLibraryConnectionHolder = mock()
    private val collector = BillingPurchaseCollector(mock<UtilsProvider>())
    private val params: QueryPurchasesParams = mock()

    @get:Rule
    val purchaseQueryBuilder = MockedConstructionRule(QueryPurchasesParams.Builder::class.java) { builder, _ ->
        whenever(builder.setProductType(any())).thenReturn(builder)
        whenever(builder.build()).thenReturn(params)
    }

    @Test
    fun `registers both purchase listeners before starting queries`() {
        collector.collect(BillingConfig(41, 42), billingClient, holder)

        val calls = inOrder(holder, billingClient)
        calls.verify(holder, times(2)).addListener(any())
        calls.verify(billingClient, times(2)).queryPurchasesAsync(eq(params), any())

        val builders = purchaseQueryBuilder.constructionMock.constructed()
        verify(builders[0]).setProductType(BillingClient.ProductType.INAPP)
        verify(builders[1]).setProductType(BillingClient.ProductType.SUBS)
    }

    @Test
    fun `second purchase query starts when first query throws`() {
        doThrow(IllegalStateException("First query failed"))
            .doNothing()
            .whenever(billingClient)
            .queryPurchasesAsync(any<QueryPurchasesParams>(), any())

        collector.collect(BillingConfig(41, 42), billingClient, holder)

        val builders = purchaseQueryBuilder.constructionMock.constructed()
        verify(builders[0]).setProductType(BillingClient.ProductType.INAPP)
        verify(builders[1]).setProductType(BillingClient.ProductType.SUBS)
        verify(billingClient, times(2)).queryPurchasesAsync(eq(params), any())
        verify(holder).markFailed()
        verify(holder).removeListener(any())
    }
}
