package io.appmetrica.analytics.billingv6.impl.library

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BillingLibraryConnectionHolderTest : CommonTest() {

    private val billingClient: BillingClient = mock()

    private val holder = BillingLibraryConnectionHolder(
        billingClient
    )

    @Test
    fun listeners() {
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
