package io.appmetrica.analytics.billingv4.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class PurchaseResponseListenerImplTest {

    @Mock
    private lateinit var workerExecutor: Executor
    @Mock
    private lateinit var uiExecutor: Executor
    @Mock
    private lateinit var utilsProvider: UtilsProvider
    @Mock
    private lateinit var billingInfoSender: BillingInfoSender
    @Mock
    private lateinit var billingInfoSentListener: () -> Unit
    @Mock
    private lateinit var billingLibraryConnectionHolder: BillingLibraryConnectionHolder

    private lateinit var purchaseResponseListener: PurchaseResponseListenerImpl

    private val purchaseHistoryRecords = listOf(
        PurchaseHistoryRecord("{\"productId\":\"sku1\", \"quantity\":\"2\", \"purchaseTime\":\"10\", \"purchaseToken\":\"token1\"}", "purchaseHistoryRecordsSignature1"),
        PurchaseHistoryRecord("{\"productId\":\"sku2\", \"quantity\":\"4\", \"purchaseTime\":\"11\", \"purchaseToken\":\"token2\"}", "purchaseHistoryRecordsSignature2"),
        PurchaseHistoryRecord("{\"productId\":\"sku4\", \"purchaseTime\":\"12\", \"purchaseToken\":\"token3\"}", "purchaseHistoryRecordsSignature3"),
    )
    private val skuDetails = listOf(
        SkuDetails("{\"productId\":\"sku1\", \"type\":\"inapp\", \"price_amount_micros\":1, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 3, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}"),
        SkuDetails("{\"productId\":\"sku2\", \"type\":\"subs\", \"price_amount_micros\":2, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 4, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}"),
        SkuDetails("{\"productId\":\"sku3\", \"type\":\"type\", \"price_amount_micros\":3, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 5, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}"),
        SkuDetails("{\"productId\":\"sku4\", \"type\":\"inapp\", \"price_amount_micros\":4, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 6, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}")
    )
    private val productInfos = listOf(
        ProductInfo(
            ProductType.INAPP,
            "sku1",
            2,
            1,
            "by",
            3,
            Period(
                1,
                Period.TimeUnit.DAY
            ),
            1,
            Period(
                1,
                Period.TimeUnit.MONTH
            ),
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
            2,
            "by",
            4,
            Period(
                1,
                Period.TimeUnit.DAY
            ),
            1,
            Period(
                1,
                Period.TimeUnit.MONTH
            ),
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
            6,
            Period(
                1,
                Period.TimeUnit.DAY
            ),
            1,
            Period(
                1,
                Period.TimeUnit.MONTH
            ),
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

        purchaseResponseListener = PurchaseResponseListenerImpl(
            BillingClient.SkuType.INAPP,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            skuDetails,
            billingLibraryConnectionHolder
        )
    }

    @Test
    fun testOnSkuDetailsResponseIfError() {
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
    }

    @Test
    fun testOnSkuDetailsResponseIfOk() {
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
    }
}
