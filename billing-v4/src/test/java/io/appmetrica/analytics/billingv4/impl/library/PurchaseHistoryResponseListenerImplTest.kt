package io.appmetrica.analytics.billingv4.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class PurchaseHistoryResponseListenerImplTest {

    @Mock
    private lateinit var workerExecutor: Executor
    @Mock
    private lateinit var uiExecutor: Executor
    @Mock
    private lateinit var billingClient: BillingClient
    @Mock
    private lateinit var utilsProvider: UtilsProvider
    @Mock
    private lateinit var updatePolicy: UpdatePolicy
    @Mock
    private lateinit var billingInfoManager: BillingInfoManager
    @Mock
    private lateinit var billingLibraryConnectionHolder: BillingLibraryConnectionHolder

    private lateinit var purchaseHistoryResponseListener: PurchaseHistoryResponseListenerImpl
    private val billingConfig = BillingConfig(41, 42)

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
        `when`(utilsProvider.updatePolicy).thenReturn(updatePolicy)
        `when`(utilsProvider.billingInfoManager).thenReturn(billingInfoManager)
        `when`(utilsProvider.workerExecutor).thenReturn(workerExecutor)
        `when`(utilsProvider.uiExecutor).thenReturn(uiExecutor)

        purchaseHistoryResponseListener = PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.SkuType.SUBS,
            billingLibraryConnectionHolder
        )
    }

    @Test
    fun testOnPurchaseHistoryResponseIfError() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoManager)
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener)
    }

    @Test
    fun testOnPurchaseHistoryResponseIfNullList() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoManager)
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener)
    }

    @Test
    fun testOnPurchaseHistoryResponseIfOk() {
        `when`(billingClient.isReady).thenReturn(true)
        `when`(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any()))
            .thenReturn(getBillingInfoToUpdate(ProductType.SUBS))
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            getPurchaseHistory()
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()

        verify(updatePolicy).getBillingInfoToUpdate(
            eq(billingConfig),
            captor.capture(),
            eq(billingInfoManager)
        )
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.SUBS)
        )
        verifyNoInteractions(billingInfoManager)
        verify(billingLibraryConnectionHolder).addListener(
            any<SkuDetailsResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder, never()).removeListener(
            any<SkuDetailsResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder).removeListener(
            purchaseHistoryResponseListener
        )
        verify(billingClient).querySkuDetailsAsync(
            any(),
            any()
        )
    }

    @Test
    fun testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckOccurred() {
        `when`(billingClient.isReady).thenReturn(true)
        `when`(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(true)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            getPurchaseHistory()
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.SUBS)
        )
        verify(billingInfoManager, never()).markFirstInappCheckOccurred()
    }

    @Test
    fun testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeInapp() {
        purchaseHistoryResponseListener = PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.SkuType.INAPP,
            billingLibraryConnectionHolder
        )
        `when`(billingClient.isReady).thenReturn(true)
        `when`(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            getPurchaseHistory()
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.INAPP)
        )
        verify(billingInfoManager).markFirstInappCheckOccurred()
    }

    @Test
    fun testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeNotInapp() {
        purchaseHistoryResponseListener = PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.SkuType.SUBS,
            billingLibraryConnectionHolder
        )
        `when`(billingClient.isReady).thenReturn(true)
        `when`(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        `when`(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            getPurchaseHistory()
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.SUBS)
        )
        verify(billingInfoManager, never()).markFirstInappCheckOccurred()
    }

    private fun getPurchaseHistory(): List<PurchaseHistoryRecord> {
        val purchaseHistoryRecords: MutableList<PurchaseHistoryRecord> = ArrayList()
        purchaseHistoryRecords.add(
            PurchaseHistoryRecord(
                JSONObject()
                    .put("productId", "sku1")
                    .put("purchaseToken", "token1")
                    .put("purchaseTime", 10)
                    .toString(),
                "signature"
            )
        )
        return purchaseHistoryRecords
    }

    private fun getBillingInfoToUpdate(type: ProductType): Map<String, BillingInfo> {
        return mapOf(
            "sku1" to BillingInfo(type, "sku1", "token1", 10, 0)
        )
    }
}
