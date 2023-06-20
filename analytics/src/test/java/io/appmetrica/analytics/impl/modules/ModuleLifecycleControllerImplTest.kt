package io.appmetrica.analytics.impl.modules

import android.content.Intent
import io.appmetrica.analytics.impl.AppMetricaServiceLifecycle
import io.appmetrica.analytics.modulesapi.internal.ModuleLifecycleObserver
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

class ModuleLifecycleControllerImplTest : CommonTest() {

    private val serviceLifecycle = mock<AppMetricaServiceLifecycle>()
    private val moduleLifecycleObserver = mock<ModuleLifecycleObserver>()
    private val intent = mock<Intent>()

    private val serviceLifecycleObserverCaptor = argumentCaptor<AppMetricaServiceLifecycle.LifecycleObserver>()

    private val moduleLifecycleControllerImpl = ModuleLifecycleControllerImpl(serviceLifecycle)

    @Test
    fun registerOnFirstClientConnectedObserver() {
        moduleLifecycleControllerImpl.registerObserver(moduleLifecycleObserver)
        verifyZeroInteractions(moduleLifecycleObserver)
        verify(serviceLifecycle).addFirstClientConnectObserver(serviceLifecycleObserverCaptor.capture())
        assertThat(serviceLifecycleObserverCaptor.allValues.size).isEqualTo(1)
        serviceLifecycleObserverCaptor.firstValue.onEvent(intent)
        verify(moduleLifecycleObserver).onFirstClientConnected()
    }

    @Test
    fun registerOnAllClientsDisconnectedObserver() {
        moduleLifecycleControllerImpl.registerObserver(moduleLifecycleObserver)
        verifyZeroInteractions(moduleLifecycleObserver)
        verify(serviceLifecycle).addAllClientDisconnectedObserver(serviceLifecycleObserverCaptor.capture())
        assertThat(serviceLifecycleObserverCaptor.allValues.size).isEqualTo(1)
        serviceLifecycleObserverCaptor.firstValue.onEvent(intent)
        verify(moduleLifecycleObserver).onAllClientsDisconnected()
    }
}
