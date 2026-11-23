package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.BillingClient.ProductType.SUBS
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billingv8.impl.storage.BillingInfoManagerImpl
import io.appmetrica.analytics.billingv8.impl.update.UpdatePolicyImpl
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BillingCollectionLifecycleTest : CommonTest() {

    private val purchaseTypes = mutableMapOf<QueryPurchasesParams, String>()

    @get:Rule
    val purchaseQueryBuilder = MockedConstructionRule(QueryPurchasesParams.Builder::class.java) { builder, _ ->
        val params: QueryPurchasesParams = mock()
        whenever(builder.setProductType(any())).thenAnswer {
            purchaseTypes[params] = it.getArgument(0)
            builder
        }
        whenever(builder.build()).thenReturn(params)
    }

    @Test
    fun `collects purchases and subscriptions in either completion order`() {
        for (firstType in listOf(INAPP, SUBS)) {
            val fixture = CollectionFixture()
            val first = if (firstType == INAPP) fixture.purchase else fixture.subscription
            val second = if (firstType == INAPP) fixture.subscription else fixture.purchase
            val attempt = fixture.start()

            attempt.replyWithHistory(first)
            attempt.replyWithHistory(second)
            attempt.replyWithDetails(first)
            attempt.replyWithPurchase(first)
            assertThat(fixture.sent).containsExactly(first.expectedReport())
            assertThat(fixture.storage.saved.map { it.productId }).containsExactly(first.sku)
            verify(attempt.client, never()).endConnection()

            attempt.replyWithDetails(second)
            val lastCallback = attempt.replyWithPurchase(second)
            lastCallback.onQueryPurchasesResponse(ok, listOf(second.purchase))

            assertThat(fixture.sent).containsExactly(first.expectedReport(), second.expectedReport())
            assertThat(fixture.storage.saved.map { it.purchaseToken })
                .containsExactlyInAnyOrder(first.token, second.token)
            assertThat(fixture.storage.saved).allSatisfy { assertThat(it.sendTime).isPositive() }
            assertThat(fixture.storage.firstInappChecked).isTrue()
            verify(attempt.client).endConnection()
        }
    }

    @Test
    fun `failed purchase query preserves subscription and later purchase collection`() {
        val fixture = CollectionFixture()
        val firstAttempt = fixture.start()
        firstAttempt.replyToHistory(INAPP, error, emptyList())
        verify(firstAttempt.client, never()).endConnection()
        firstAttempt.collect(fixture.subscription)
        assertThat(fixture.sent).containsExactly(fixture.subscription.expectedReport())
        assertThat(fixture.storage.firstInappChecked).isFalse()
        assertThat(fixture.storage.saved.map { it.productId }).containsExactly(fixture.subscription.sku)
        verify(firstAttempt.client).endConnection()

        val subscriptionSendTime = fixture.storage.saved.single().sendTime
        fixture.now = subscriptionSendTime + 59_999
        val retry = fixture.start()
        retry.replyWithHistory(fixture.subscription)
        retry.collect(fixture.purchase)
        assertThat(fixture.sent)
            .containsExactly(fixture.subscription.expectedReport(), fixture.purchase.expectedReport())
        assertThat(fixture.storage.firstInappChecked).isTrue()
        assertThat(fixture.storage.saved.first { it.type == ProductType.SUBS }.sendTime).isEqualTo(subscriptionSendTime)
        verify(retry.client).endConnection()

        fixture.now = subscriptionSendTime + 60_000
        val nextAttempt = fixture.start()
        nextAttempt.replyWithHistory(fixture.purchase)
        nextAttempt.collect(fixture.subscription)
        assertThat(fixture.sent).containsExactly(
            fixture.subscription.expectedReport(),
            fixture.purchase.expectedReport(),
            fixture.subscription.expectedReport(),
        )
        val calls = inOrder(retry.client, nextAttempt.client)
        calls.verify(retry.client).endConnection()
        calls.verify(nextAttempt.client).startConnection(any())
        verify(nextAttempt.client).endConnection()
    }

    @Test
    fun `reporting exception leaves the purchase eligible for retry`() {
        val fixture = CollectionFixture()
        val attempt = fixture.start()
        fixture.failingSku = fixture.purchase.sku
        attempt.collect(fixture.purchase)

        assertThat(fixture.sent).isEmpty()
        assertThat(fixture.storage.saved).isEmpty()
        assertThat(fixture.storage.firstInappChecked).isFalse()
        verify(attempt.client, never()).endConnection()

        attempt.collect(fixture.subscription)
        assertThat(fixture.sent).containsExactly(fixture.subscription.expectedReport())
        verify(attempt.client).endConnection()

        fixture.failingSku = null
        val retry = fixture.start()
        retry.replyWithHistory(fixture.subscription)
        retry.collect(fixture.purchase)
        assertThat(fixture.sent)
            .containsExactly(fixture.subscription.expectedReport(), fixture.purchase.expectedReport())
        assertThat(fixture.storage.firstInappChecked).isTrue()
        verify(retry.client).endConnection()
    }

    @Test
    fun `queued foreground collects a new token once across reconnect`() {
        val fixture = CollectionFixture()
        val purchaseA = fixture.purchase
        val purchaseB = TestProduct(INAPP, purchaseA.sku, fixture.now, "replacement-token")
        val attempt = fixture.start()
        attempt.collect(purchaseA)

        fixture.start()
        fixture.start()
        assertThat(attempt.pendingPurchaseTypes).containsExactly(SUBS)
        assertThat(fixture.sent).containsExactly(purchaseA.expectedReport())

        attempt.collect(fixture.subscription)
        attempt.collect(purchaseB)
        attempt.replyWithHistory(fixture.subscription)
        assertThat(attempt.pendingPurchaseTypes).isEmpty()
        verify(attempt.client).endConnection()

        val nextAttempt = fixture.start()
        val calls = inOrder(attempt.client, nextAttempt.client)
        calls.verify(attempt.client).endConnection()
        calls.verify(nextAttempt.client).startConnection(any())
        nextAttempt.replyWithHistory(purchaseB)
        nextAttempt.replyWithHistory(fixture.subscription)

        assertThat(fixture.sent).containsExactly(
            purchaseA.expectedReport(),
            fixture.subscription.expectedReport(),
            purchaseB.expectedReport(),
        )
        assertThat(fixture.storage.saved.map { it.purchaseToken })
            .containsExactlyInAnyOrder(purchaseB.token, fixture.subscription.token)
        assertThat(nextAttempt.pendingPurchaseTypes).isEmpty()
        verify(nextAttempt.client).endConnection()
    }

    @Test
    fun `late purchase callback from disabled collection cannot affect the next collection`() {
        val fixture = CollectionFixture()
        val oldPurchase = fixture.purchase
        val newPurchase = TestProduct(INAPP, oldPurchase.sku, fixture.now, "replacement-token")
        val oldAttempt = fixture.start()
        oldAttempt.replyWithHistory(oldPurchase)
        oldAttempt.replyWithDetails(oldPurchase)
        fixture.start()

        fixture.disable()
        val newAttempt = fixture.start()
        val calls = inOrder(oldAttempt.client, newAttempt.client)
        calls.verify(oldAttempt.client).endConnection()
        calls.verify(newAttempt.client).startConnection(any())

        oldAttempt.replyWithPurchase(oldPurchase)
        assertThat(fixture.sent).isEmpty()
        assertThat(fixture.storage.saved).isEmpty()
        assertThat(fixture.storage.firstInappChecked).isFalse()

        newAttempt.collect(newPurchase)
        newAttempt.collect(fixture.subscription)
        assertThat(fixture.sent).containsExactly(newPurchase.expectedReport(), fixture.subscription.expectedReport())
        assertThat(fixture.storage.saved.map { it.purchaseToken })
            .containsExactlyInAnyOrder(newPurchase.token, fixture.subscription.token)
        assertThat(newAttempt.pendingPurchaseTypes).isEmpty()
        verify(oldAttempt.client).endConnection()
        verify(newAttempt.client).endConnection()
    }

    private inner class CollectionFixture {
        var now = System.currentTimeMillis()
        var failingSku: String? = null
        val purchase = TestProduct(INAPP, "item", now)
        val subscription = TestProduct(SUBS, "subscription", now)
        val storage = RecordingStorage()
        val sent = mutableListOf<ProductInfo>()

        private val manager = BillingInfoManagerImpl(storage)
        private val clock: SystemTimeProvider = mock {
            on { currentTimeMillis() } doAnswer { now }
        }
        private val worker: IHandlerExecutor = mock {
            on { post(any()) } doAnswer {
                it.getArgument<Runnable>(0).run()
                null
            }
        }
        private val policy = UpdatePolicyImpl(clock)
        private val sender = BillingInfoSender { reports ->
            check(reports.none { it.sku == failingSku })
            sent.addAll(reports)
        }
        private val utils: UtilsProvider = mock {
            on { billingInfoManager } doReturn manager
            on { updatePolicy } doReturn policy
            on { workerExecutor } doReturn worker
            on { billingInfoSender } doReturn sender
        }

        private var latestAttempt: Attempt? = null
        private val collector = BillingPurchaseCollector(utils)
        private val controller = BillingConnectionController(
            workerExecutor = worker,
            createClient = { Attempt().also { latestAttempt = it }.client },
            collect = collector::collect,
        )

        fun start(): Attempt {
            controller.request(BillingConfig(60, 120))
            return checkNotNull(latestAttempt).also { it.finishSetup() }
        }

        fun disable() = controller.disable()
    }

    private inner class Attempt {
        val client: BillingClient = mock()

        private var setup: BillingClientStateListener? = null
        private val purchaseCallbacks = mutableMapOf<String, PurchasesResponseListener>()
        private val detailsCallbacks = ArrayDeque<ProductDetailsResponseListener>()
        val pendingPurchaseTypes: Set<String>
            get() = purchaseCallbacks.keys

        init {
            whenever(client.isReady).thenReturn(true)
            doAnswer {
                setup = it.getArgument(0)
                null
            }.whenever(client).startConnection(any())
            doAnswer {
                purchaseCallbacks[purchaseTypes.getValue(it.getArgument(0))] = it.getArgument(1)
                null
            }.whenever(client).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
            doAnswer {
                detailsCallbacks.addLast(it.getArgument(1))
                null
            }.whenever(client).queryProductDetailsAsync(any(), any())
        }

        fun finishSetup() {
            val listener = setup ?: return
            setup = null
            listener.onBillingSetupFinished(ok)
        }

        fun collect(product: TestProduct) {
            replyWithHistory(product)
            replyWithDetails(product)
            replyWithPurchase(product)
        }

        fun replyToHistory(type: String, result: BillingResult, purchases: List<Purchase>) {
            purchaseCallbacks.remove(type)!!.onQueryPurchasesResponse(result, purchases)
        }

        fun replyWithHistory(product: TestProduct) = replyToHistory(product.type, ok, listOf(product.purchase))

        fun replyWithDetails(product: TestProduct) {
            detailsCallbacks.removeFirst().onProductDetailsResponse(ok, product.details)
        }

        fun replyWithPurchase(product: TestProduct): PurchasesResponseListener =
            purchaseCallbacks.remove(product.type)!!.also { it.onQueryPurchasesResponse(ok, listOf(product.purchase)) }
    }

    private class RecordingStorage : BillingInfoStorage {
        var saved = emptyList<BillingInfo>()
        var firstInappChecked = false

        override fun getBillingInfo(): List<BillingInfo> = saved

        override fun isFirstInappCheckOccurred(): Boolean = firstInappChecked

        override fun saveInfo(billingInfos: List<BillingInfo>, firstInappCheckOccurred: Boolean) {
            saved = billingInfos
            firstInappChecked = firstInappCheckOccurred
        }
    }

    private class TestProduct(
        val type: String,
        val sku: String,
        private val time: Long,
        val token: String = "token-$sku",
    ) {
        private val signatureValue = "signature-$sku"
        private val json = """{"productId":"$sku"}"""
        private val renewing = type == SUBS
        private val price = if (type == INAPP) 1_290_000L else 0L
        private val currency = if (type == INAPP) "USD" else ""

        val purchase: Purchase = mock {
            on { products } doReturn listOf(sku)
            on { purchaseToken } doReturn token
            on { purchaseTime } doReturn time
            on { quantity } doReturn 1
            on { signature } doReturn signatureValue
            on { originalJson } doReturn json
            on { isAutoRenewing } doReturn renewing
        }

        private val offer: ProductDetails.OneTimePurchaseOfferDetails = mock {
            on { priceAmountMicros } doReturn price
            on { priceCurrencyCode } doReturn currency
        }
        private val productDetails: ProductDetails = mock {
            on { productId } doReturn sku
            on { productType } doReturn type
            on { oneTimePurchaseOfferDetails } doReturn if (type == INAPP) offer else null
        }
        val details: QueryProductDetailsResult = mock {
            on { productDetailsList } doReturn listOf(productDetails)
        }

        fun expectedReport() = ProductInfo(
            if (type == INAPP) ProductType.INAPP else ProductType.SUBS,
            sku, 1, price, currency, 0, null, 1, null, signatureValue, token, time, renewing, json,
        )
    }

    private companion object {
        val ok: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
        val error: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.ERROR)
            .build()
    }
}
