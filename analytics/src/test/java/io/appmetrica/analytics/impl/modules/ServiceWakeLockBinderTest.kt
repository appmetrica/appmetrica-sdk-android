package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ServiceWakeLockBinderTest : CommonTest() {

    private val context = mock<Context>()
    private val action = "Some action"
    private val intent = mock<Intent>()
    private val intentProvider = mock<ServiceWakeLockIntentProvider> {
        on { getWakeLockIntent(context, action) } doReturn intent
    }
    private val serviceConnectionArgumentCaptor = argumentCaptor<ServiceConnection>()

    private val serviceWakeLockBinder = ServiceWakeLockBinder(intentProvider)

    @Test
    fun bindService() {
        val result = serviceWakeLockBinder.bindService(context, action)
        verify(context).bindService(eq(intent), serviceConnectionArgumentCaptor.capture(), eq(Context.BIND_AUTO_CREATE))
        assertThat(result).isEqualTo(serviceConnectionArgumentCaptor.firstValue)
    }

    @Test
    fun bindServiceIfFailed() {
        whenever(context.bindService(any(), any(), anyInt())).thenThrow(RuntimeException())
        assertThat(serviceWakeLockBinder.bindService(context, action)).isNull()
    }

    @Test
    fun unbindService() {
        serviceWakeLockBinder.bindService(context, action)
        verify(context).bindService(eq(intent), serviceConnectionArgumentCaptor.capture(), eq(Context.BIND_AUTO_CREATE))
        serviceWakeLockBinder.unbindService(action, context, serviceConnectionArgumentCaptor.firstValue)
        verify(context).unbindService(serviceConnectionArgumentCaptor.firstValue)
    }

    @Test
    fun unbindServiceIfException() {
        serviceWakeLockBinder.bindService(context, action)
        whenever(context.unbindService(any())).thenThrow(RuntimeException())
        serviceWakeLockBinder.unbindService(action, context, mock<ServiceConnection>())
    }
}
