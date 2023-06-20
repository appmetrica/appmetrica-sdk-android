package io.appmetrica.analytics.billingv4.impl.library

import android.os.Handler
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BillingLibraryConnectionHolderTest {

    private lateinit var holder: BillingLibraryConnectionHolder

    @Mock
    private lateinit var billingClient: BillingClient
    @Mock
    private lateinit var mainHandler: Handler

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.`when`(mainHandler).post(any<SafeRunnable>())

        holder = BillingLibraryConnectionHolder(
            billingClient,
            mainHandler
        )
    }

    @Test
    fun testListeners() {
        val listener1 = Any()
        val listener2 = Any()
        holder.addListener(listener1)
        holder.addListener(listener2)
        verify(billingClient, never()).endConnection()
        holder.removeListener(listener1)
        verify(billingClient, never()).endConnection()
        holder.removeListener(listener2)
        verify(billingClient).endConnection()
    }
}
