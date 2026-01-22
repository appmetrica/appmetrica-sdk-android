package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billingv8.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
internal class ProductDetailsResponseListenerImplTest : CommonTest() {

    private val workerExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val billingClient: BillingClient = mock()
    private val billingInfoSender: BillingInfoSender = mock()
    private val utilsProvider: UtilsProvider = mock {
        on { billingInfoSender } doReturn billingInfoSender
        on { workerExecutor } doReturn workerExecutor
    }
    private val billingInfoSentListener: () -> Unit = mock()
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder = mock()

    private val purchaseHistoryRecord: Purchase = mock()
    private val purchaseHistoryRecords = listOf(
        purchaseHistoryRecord
    )

    private val productDetails: ProductDetails = mock()
    private val productDetailsList = listOf(
        productDetails
    )
    private val queryProductDetailsResult: QueryProductDetailsResult = mock {
        on { productDetailsList } doReturn productDetailsList
    }

    private val updateBillingProgressCallback: UpdateBillingProgressCallback = mock()

    private val emptyQueryProductDetailsResult: QueryProductDetailsResult = mock {
        on { productDetailsList } doReturn emptyList()
    }

    private val skuDetailsResponseListener by setUp {
        ProductDetailsResponseListenerImpl(
            BillingClient.ProductType.INAPP,
            billingClient,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
    }

    @Test
    fun onSkuDetailsResponseIfError() {
        skuDetailsResponseListener.onProductDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build(),
            emptyQueryProductDetailsResult
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onSkuDetailsIfNotReady() {
        whenever(billingClient.isReady).thenReturn(false)
        skuDetailsResponseListener.onProductDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            queryProductDetailsResult
        )
        verify(billingLibraryConnectionHolder, times(2)).removeListener(any())
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onSkuDetailsResponseIfOk() {
        whenever(billingClient.isReady).thenReturn(true)
        skuDetailsResponseListener.onProductDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            queryProductDetailsResult
        )
        verify(billingLibraryConnectionHolder).addListener(
            any<PurchaseResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder, never()).removeListener(
            any<PurchaseResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder).removeListener(
            skuDetailsResponseListener
        )
        verify(billingClient).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
    }

    @Test
    fun onSkuDetailsResponseIfOkAndEmptyList() {
        skuDetailsResponseListener.onProductDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            emptyQueryProductDetailsResult
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }
}
