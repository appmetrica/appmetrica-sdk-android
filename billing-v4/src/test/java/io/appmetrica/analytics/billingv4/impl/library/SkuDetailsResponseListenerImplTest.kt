package io.appmetrica.analytics.billingv4.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class SkuDetailsResponseListenerImplTest {

    @Mock
    private lateinit var workerExecutor: Executor
    @Mock
    private lateinit var uiExecutor: Executor
    @Mock
    private lateinit var billingClient: BillingClient
    @Mock
    private lateinit var utilsProvider: UtilsProvider
    @Mock
    private lateinit var billingInfoSender: BillingInfoSender
    @Mock
    private lateinit var billingInfoMap: Map<String, BillingInfo>
    @Mock
    private lateinit var billingInfoSentListener: () -> Unit
    @Mock
    private lateinit var billingLibraryConnectionHolder: BillingLibraryConnectionHolder

    private lateinit var skuDetailsResponseListener: SkuDetailsResponseListenerImpl

    private val purchaseHistoryRecords = listOf(
        PurchaseHistoryRecord("{\"productId\":\"sku1\", \"quantity\":\"2\", \"purchaseTime\":\"10\", \"purchaseToken\":\"token1\"}", "purchaseHistoryRecordsSignature1")
    )

    private val skuDetailsList = listOf(
        SkuDetails("{\"productId\":\"sku1\", \"type\":\"inapp\", \"price_amount_micros\":1, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 3, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.`when`(workerExecutor).execute(any<SafeRunnable>())
        doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.`when`(uiExecutor).execute(any<SafeRunnable>())
        `when`(utilsProvider.billingInfoSender).thenReturn(billingInfoSender)
        `when`(utilsProvider.workerExecutor).thenReturn(workerExecutor)
        `when`(utilsProvider.uiExecutor).thenReturn(uiExecutor)

        skuDetailsResponseListener = SkuDetailsResponseListenerImpl(
            BillingClient.SkuType.INAPP,
            billingClient,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            billingLibraryConnectionHolder
        )
    }

    @Test
    fun testOnSkuDetailsResponseIfError() {
        skuDetailsResponseListener.onSkuDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
    }

    @Test
    fun testOnSkuDetailsResponseIfOk() {
        `when`(billingClient.isReady).thenReturn(true)
        skuDetailsResponseListener.onSkuDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            skuDetailsList
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
        verify(billingClient).queryPurchasesAsync(any(), any())
    }

    @Test
    fun testOnSkuDetailsResponseIfOkAndNullList() {
        skuDetailsResponseListener.onSkuDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
    }

    @Test
    fun testOnSkuDetailsResponseIfOkAndEmptyList() {
        skuDetailsResponseListener.onSkuDetailsResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            listOf()
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoSender)
        verifyNoInteractions(billingInfoSentListener)
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener)
    }
}
