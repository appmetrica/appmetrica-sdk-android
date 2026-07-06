package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import org.junit.runner.RunWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@SuppressLint("RobolectricUsage")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
internal class ContextReceiverSafeWrapperTiramisuTest : ContextReceiverSafeWrapperBaseTest() {

    override fun stubRegisterReceiverReturns(result: Intent) {
        whenever(
            context.registerReceiver(broadcastReceiver, intentFilter, null, handler, Context.RECEIVER_NOT_EXPORTED)
        ).thenReturn(result)
    }

    override fun stubRegisterReceiverThrows(throwable: Throwable) {
        whenever(
            context.registerReceiver(broadcastReceiver, intentFilter, null, handler, Context.RECEIVER_NOT_EXPORTED)
        ).thenThrow(throwable)
    }

    override fun verifyRegisterReceiver(times: Int) {
        verify(context, times(times))
            .registerReceiver(broadcastReceiver, intentFilter, null, handler, Context.RECEIVER_NOT_EXPORTED)
    }
}
