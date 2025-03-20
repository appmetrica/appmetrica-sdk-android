package io.appmetrica.analytics.billingv6.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.billingv6.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.billingv6.impl.storage.StorageUpdater
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class PurchaseHistoryResponseListenerImplTest : CommonTest() {

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
    private val updatePolicy: UpdatePolicy = mock()
    private val billingInfoManager: BillingInfoManager = mock()
    private val utilsProvider: UtilsProvider = mock {
        on { billingInfoManager } doReturn billingInfoManager
        on { uiExecutor } doReturn uiExecutor
        on { updatePolicy } doReturn updatePolicy
        on { workerExecutor } doReturn workerExecutor
    }
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder = mock()

    private val purchaseHistoryRecord: PurchaseHistoryRecord = mock {
        on { products } doReturn listOf("sku1")
        on { purchaseToken } doReturn "token1"
        on { purchaseTime } doReturn 10
        on { signature } doReturn "signature"
    }
    private val purchaseHistoryRecords = listOf(
        purchaseHistoryRecord
    )

    private val updateBillingProgressCallback: UpdateBillingProgressCallback = mock()

    private val billingConfig = BillingConfig(41, 42)
    private val purchaseHistoryResponseListener by setUp {
        PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.ProductType.SUBS,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
    }

    @Test
    fun onPurchaseHistoryResponseIfBillingLibraryNotReady() {
        whenever(billingClient.isReady).thenReturn(false)
        whenever(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any()))
            .thenReturn(getBillingInfoToUpdate(ProductType.SUBS))
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchaseHistoryRecords
        )
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfError() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoManager)
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfNullList() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            null
        )
        verifyNoInteractions(billingClient)
        verifyNoInteractions(billingInfoManager)
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener)
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfOk() {
        whenever(billingClient.isReady).thenReturn(true)
        whenever(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any()))
            .thenReturn(getBillingInfoToUpdate(ProductType.SUBS))
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchaseHistoryRecords
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
            any<ProductDetailsResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder, never()).removeListener(
            any<ProductDetailsResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder).removeListener(
            purchaseHistoryResponseListener
        )
        verify(billingClient).queryProductDetailsAsync(
            any(),
            any()
        )
        verify(updateBillingProgressCallback, never()).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckOccurred() {
        whenever(billingClient.isReady).thenReturn(true)
        whenever(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(true)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchaseHistoryRecords
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.SUBS)
        )
        verify(billingInfoManager, never()).markFirstInappCheckOccurred()
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeInapp() {
        val purchaseHistoryResponseListener = PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.ProductType.INAPP,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
        whenever(billingClient.isReady).thenReturn(true)
        whenever(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchaseHistoryRecords
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.INAPP)
        )
        verify(billingInfoManager).markFirstInappCheckOccurred()
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    @Test
    fun onPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeNotInapp() {
        val purchaseHistoryResponseListener = PurchaseHistoryResponseListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            BillingClient.ProductType.SUBS,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
        whenever(billingClient.isReady).thenReturn(true)
        whenever(updatePolicy.getBillingInfoToUpdate(any(), anyMap(), any())).thenReturn(mapOf())
        whenever(billingInfoManager.isFirstInappCheckOccurred).thenReturn(false)
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build(),
            purchaseHistoryRecords
        )
        val captor = argumentCaptor<Map<String, BillingInfo>>()
        verify(billingInfoManager).update(captor.capture())
        assertThat(captor.firstValue).usingRecursiveComparison().isEqualTo(
            getBillingInfoToUpdate(ProductType.SUBS)
        )
        verify(billingInfoManager, never()).markFirstInappCheckOccurred()
        verify(updateBillingProgressCallback).onUpdateFinished()
    }

    private fun getBillingInfoToUpdate(type: ProductType): Map<String, BillingInfo> {
        return mapOf(
            "sku1" to BillingInfo(type, "sku1", "token1", 10, 0)
        )
    }
}
