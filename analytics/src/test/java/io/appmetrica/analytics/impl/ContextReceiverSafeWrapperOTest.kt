package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import org.junit.runner.RunWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RECEIVER_NOT_EXPORTED_COMPAT = 4

@SuppressLint("RobolectricUsage", "WrongConstant")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
internal class ContextReceiverSafeWrapperOTest : ContextReceiverSafeWrapperBaseTest() {

    override fun stubRegisterReceiverReturns(result: Intent) {
        whenever(context.registerReceiver(broadcastReceiver, intentFilter, null, handler, RECEIVER_NOT_EXPORTED_COMPAT))
            .thenReturn(result)
    }

    override fun stubRegisterReceiverThrows(throwable: Throwable) {
        whenever(context.registerReceiver(broadcastReceiver, intentFilter, null, handler, RECEIVER_NOT_EXPORTED_COMPAT))
            .thenThrow(throwable)
    }

    override fun verifyRegisterReceiver(times: Int) {
        verify(context, times(times))
            .registerReceiver(broadcastReceiver, intentFilter, null, handler, RECEIVER_NOT_EXPORTED_COMPAT)
    }
}
