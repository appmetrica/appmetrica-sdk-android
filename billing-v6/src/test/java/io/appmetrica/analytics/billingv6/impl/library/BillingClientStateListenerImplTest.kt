package io.appmetrica.analytics.billingv6.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryPurchaseHistoryParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingClientStateListenerImplTest : CommonTest() {

    private val workerExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val uiExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val billingClient: BillingClient = mock()
    private val utilsProvider: UtilsProvider = mock {
        on { uiExecutor } doReturn uiExecutor
        on { workerExecutor } doReturn workerExecutor
    }
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder = mock()
    private val queryPurchaseHistoryParams: QueryPurchaseHistoryParams = mock()

    @get:Rule
    val queryPurchaseHistoryParamsBuilderRule = MockedConstructionRule(
        QueryPurchaseHistoryParams.Builder::class.java
    ) { mock, _ ->
        whenever(mock.setProductType(any())).thenReturn(mock)
        whenever(mock.build()).thenReturn(queryPurchaseHistoryParams)
    }

    private val billingConfig = BillingConfig(41, 42)
    private val billingClientStateListener = BillingClientStateListenerImpl(
        billingConfig,
        billingClient,
        utilsProvider,
        billingLibraryConnectionHolder
    )

    @Test
    fun onBillingSetupFinishedIfError() {
        billingClientStateListener.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build()
        )
        verify(workerExecutor).execute(any())
        verify(uiExecutor, never()).execute(any())
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingLibraryConnectionHolder)
    }

    @Test
    fun onBillingSetupFinishedIfOk() {
        whenever(billingClient.isReady).thenReturn(true)
        billingClientStateListener.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        )
        verify(billingLibraryConnectionHolder, times(2)).addListener(any())
        verify(billingClient, times(2)).queryPurchaseHistoryAsync(
            eq(queryPurchaseHistoryParams),
            any<PurchaseHistoryResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder, never()).removeListener(any())
        val builders = queryPurchaseHistoryParamsBuilderRule.constructionMock.constructed()
        verify(builders[0]).setProductType(BillingClient.ProductType.INAPP)
        verify(builders[1]).setProductType(BillingClient.ProductType.SUBS)
    }

    @Test
    fun onSessionResumedIfBillingClientIsNotReady() {
        whenever(billingClient.isReady).thenReturn(false)
        billingClientStateListener.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        )
        verify(billingLibraryConnectionHolder, times(2)).addListener(any())
        verify(billingLibraryConnectionHolder, times(2)).removeListener(any())
        verify(billingClient, never()).queryPurchaseHistoryAsync(any<QueryPurchaseHistoryParams>(), any())
    }
}
