package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.ServiceConnection
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ServiceWakeLockImplTest : CommonTest() {

    private val wakeLockId = "some-wake-lock-id"
    private val context = mock<Context>()
    private val serviceConnection = mock<ServiceConnection>()
    private val serviceWakeLockBinder = mock<ServiceWakeLockBinder> {
        on { bindService(context, action(wakeLockId)) } doReturn serviceConnection
    }

    private val serviceWakeLockImpl = ServiceWakeLockImpl(context, serviceWakeLockBinder)

    @Test
    fun acquireWakeLock() {
        assertThat(serviceWakeLockImpl.acquireWakeLock(wakeLockId)).isTrue()
        assertThat(serviceWakeLockImpl.acquireWakeLock(wakeLockId)).isTrue()
        verify(serviceWakeLockBinder).bindService(context, action(wakeLockId))
    }

    @Test
    fun acquireWakeLockIfCouldNotBind() {
        whenever(serviceWakeLockBinder.bindService(context, action(wakeLockId)))
            .thenReturn(null)
        assertThat(serviceWakeLockImpl.acquireWakeLock(wakeLockId)).isFalse()
        whenever(serviceWakeLockBinder.bindService(context, action(wakeLockId)))
            .thenReturn(serviceConnection)
        assertThat(serviceWakeLockImpl.acquireWakeLock(wakeLockId)).isTrue()
    }

    @Test
    fun releaseWakeLockWithoutAcquire() {
        serviceWakeLockImpl.releaseWakeLock(wakeLockId)
        verifyNoMoreInteractions(serviceWakeLockBinder)
    }

    @Test
    fun releaseWakeLock() {
        serviceWakeLockImpl.acquireWakeLock(wakeLockId)
        serviceWakeLockImpl.releaseWakeLock(wakeLockId)
        serviceWakeLockImpl.releaseWakeLock(wakeLockId)
        verify(serviceWakeLockBinder).unbindService(eq(action(wakeLockId)), eq(context), any())
    }

    private fun action(wakeLockId: String) = "io.appmetrica.analytics.ACTION_SERVICE_WAKELOCK.$wakeLockId"
}
