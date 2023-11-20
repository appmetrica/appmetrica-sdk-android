package io.appmetrica.analytics.billingv6.impl.library

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingLibraryConnectionHolderTest : CommonTest() {

    private val billingClient: BillingClient = mock()
    private val uiExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val utilsProvider: UtilsProvider = mock {
        on { uiExecutor } doReturn uiExecutor
    }

    private val holder = BillingLibraryConnectionHolder(
        billingClient,
        utilsProvider
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
