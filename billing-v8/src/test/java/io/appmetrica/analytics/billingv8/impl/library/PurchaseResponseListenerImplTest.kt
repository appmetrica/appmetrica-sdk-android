package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billingv8.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
internal class PurchaseResponseListenerImplTest : CommonTest() {

    private val workerExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val billingInfoSender: BillingInfoSender = mock()
    private val utilsProvider: UtilsProvider = mock {
        on { billingInfoSender } doReturn billingInfoSender
        on { workerExecutor } doReturn workerExecutor
    }
    private val billingInfoSentListener: () -> Unit = mock()
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder = mock()

    private val firstPurchase: Purchase = mock {
        on { products } doReturn listOf("sku1")
        on { quantity } doReturn 2
        on { purchaseTime } doReturn 10L
        on { purchaseToken } doReturn "token1"
        on { signature } doReturn "purchaseHistoryRecordsSignature1"
    }
    private val secondPurchase: Purchase = mock {
        on { products } doReturn listOf("sku2")
        on { quantity } doReturn 4
        on { purchaseTime } doReturn 11L
        on { purchaseToken } doReturn "token2"
        on { signature } doReturn "purchaseHistoryRecordsSignature2"
    }
    private val thirdPurchase: Purchase = mock {
        on { products } doReturn listOf("sku4")
        on { quantity } doReturn 1
        on { purchaseTime } doReturn 12L
        on { purchaseToken } doReturn "token3"
        on { signature } doReturn "purchaseHistoryRecordsSignature3"
    }
    private val purchaseHistoryRecords = listOf(
        firstPurchase,
        secondPurchase,
        thirdPurchase,
    )
    private val oneTimePurchaseOfferDetails1: ProductDetails.OneTimePurchaseOfferDetails = mock {
        on { priceAmountMicros } doReturn 1
        on { priceCurrencyCode } doReturn "by"
    }
    private val productDetails1: ProductDetails = mock {
        on { productId } doReturn "sku1"
        on { productType } doReturn "inapp"
        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetails1
    }
    private val productDetails2: ProductDetails = mock {
        on { productId } doReturn "sku2"
        on { productType } doReturn "subs"
    }
    private val productDetails3: ProductDetails = mock {
        on { productId } doReturn "sku3"
        on { productType } doReturn "type"
    }
    private val oneTimePurchaseOfferDetails4: ProductDetails.OneTimePurchaseOfferDetails = mock {
        on { priceAmountMicros } doReturn 4
        on { priceCurrencyCode } doReturn "by"
    }
    private val productDetails4: ProductDetails = mock {
        on { productId } doReturn "sku4"
        on { productType } doReturn "inapp"
        on { oneTimePurchaseOfferDetails } doReturn oneTimePurchaseOfferDetails4
    }
    private val skuDetails = listOf(
        productDetails1,
        productDetails2,
        productDetails3,
        productDetails4
    )
    private val productInfos = listOf(
        ProductInfo(
            ProductType.INAPP,
            "sku1",
            2,
            1,
            "by",
            0,
            null,
            1,
            null,
            "purchaseHistoryRecordsSignature1",
            "token1",
            10,
            true,
            "{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}"
        ),
        ProductInfo(
            ProductType.SUBS,
            "sku2",
            4,
            0,
            "",
            0,
            null,
            1,
            null,
            "purchaseHistoryRecordsSignature2",
            "token2",
            11,
            false,
            "{}"
        ),
        ProductInfo(
            ProductType.INAPP,
            "sku4",
            1,
            4,
            "by",
            0,
            null,
            1,
            null,
            "purchaseHistoryRecordsSignature3",
            "token3",
            12,
            false,
            "{}"
        )
    )
    private val purchases = listOf(
        Purchase("{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}", "purchasesSignature")
    )

    private val updateBillingProgressCallback: UpdateBillingProgressCallback = mock()

    private val purchaseResponseListener by setUp {
        PurchaseResponseListenerImpl(
            BillingClient.ProductType.INAPP,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            skuDetails,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
    }

    @Test
    fun onSkuDetailsResponseIfError() {
        purchaseResponseListener.onQueryPurchasesResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build(),
            listOf()
        )
        verify(billingLibraryConnectionHolder).removeListener(
            purchaseResponseListener
        )
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onSkuDetailsResponseIfOk() {
        purchaseResponseListener.onQueryPurchasesResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchases
        )
        verify(billingLibraryConnectionHolder).removeListener(
            purchaseResponseListener
        )
        val argument = argumentCaptor<List<ProductInfo>>()
        verify(billingInfoSender).sendInfo(argument.capture())
        assertThat(argument.firstValue).isEqualTo(productInfos)
        verify(billingInfoSentListener).invoke()
        verify(updateBillingProgressCallback).onUpdateFinished()
    }
}
