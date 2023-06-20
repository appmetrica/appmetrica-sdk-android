package io.appmetrica.analytics.billingv4.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingClientStateListenerImplTest {

    @Mock
    private lateinit var workerExecutor: Executor
    @Mock
    private lateinit var uiExecutor: Executor
    @Mock
    private lateinit var billingClient: BillingClient
    @Mock
    private lateinit var utilsProvider: UtilsProvider
    @Mock
    private lateinit var billingLibraryConnectionHolder: BillingLibraryConnectionHolder

    private val billingConfig = BillingConfig(41, 42)
    private lateinit var billingClientStateListener: BillingClientStateListenerImpl

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
        `when`(utilsProvider.uiExecutor).thenReturn(uiExecutor)
        `when`(utilsProvider.workerExecutor).thenReturn(workerExecutor)

        billingClientStateListener = BillingClientStateListenerImpl(
            billingConfig,
            billingClient,
            utilsProvider,
            billingLibraryConnectionHolder
        )
    }

    @Test
    fun testOnBillingSetupFinishedIfError() {
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
    fun testOnBillingSetupFinishedIfOk() {
        `when`(billingClient.isReady).thenReturn(true)
        billingClientStateListener.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        )
        verify(billingLibraryConnectionHolder, times(2)).addListener(any())
        verify(billingClient, times(1)).queryPurchaseHistoryAsync(
            eq(BillingClient.SkuType.INAPP),
            any<PurchaseHistoryResponseListenerImpl>()
        )
        verify(billingClient, times(1)).queryPurchaseHistoryAsync(
            eq(BillingClient.SkuType.SUBS),
            any<PurchaseHistoryResponseListenerImpl>()
        )
        verify(billingLibraryConnectionHolder, never()).removeListener(any())
    }

    @Test
    fun testOnSessionResumedIfBillingClientIsNotReady() {
        `when`(billingClient.isReady).thenReturn(false)
        billingClientStateListener.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        )
        verify(billingLibraryConnectionHolder, times(2)).addListener(any())
        verify(billingLibraryConnectionHolder, times(2)).removeListener(any())
        verify(billingClient, never()).queryPurchaseHistoryAsync(any(), any())
    }
}
