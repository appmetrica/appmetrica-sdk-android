package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ProductDetailsResponseListenerImplTest : CommonTest() {

    private val workerExecutor: IHandlerExecutor = mock {
        on { post(any()) } doAnswer {
            it.getArgument<Runnable>(0).run()
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
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder = mock {
        on { isWaitingFor(any()) } doReturn true
    }

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
            billingLibraryConnectionHolder
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
        val calls = inOrder(billingLibraryConnectionHolder)
        calls.verify(billingLibraryConnectionHolder).markFailed()
        calls.verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
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
        val calls = inOrder(billingLibraryConnectionHolder)
        calls.verify(billingLibraryConnectionHolder).markFailed()
        calls.verify(billingLibraryConnectionHolder).removeListener(any<PurchaseResponseListenerImpl>())
        calls.verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
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
        verify(billingLibraryConnectionHolder, never()).markFailed()
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
    }
}
