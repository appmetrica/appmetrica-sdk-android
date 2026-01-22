package io.appmetrica.analytics.impl

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Process
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppAppMetricaServiceLifecycleTests : CommonTest() {

    private val observer: AppMetricaServiceLifecycle.LifecycleObserver = mock()
    private val observer2: AppMetricaServiceLifecycle.LifecycleObserver = mock()
    private val observer3: AppMetricaServiceLifecycle.LifecycleObserver = mock()
    private val observer4: AppMetricaServiceLifecycle.LifecycleObserver = mock()
    private val observer5: AppMetricaServiceLifecycle.LifecycleObserver = mock()

    private val configuration: Configuration = mock()
    private val mockedIntent: Intent = mock()

    private val appMetricaServiceLifecycle: AppMetricaServiceLifecycle by setUp { AppMetricaServiceLifecycle() }

    @Test
    fun onCreateInvolvingOnAllObservers() {
        fillAllObservers()
        appMetricaServiceLifecycle.onCreate()
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun onStartInvolvingOnAllObservers() {
        fillAllObservers()
        appMetricaServiceLifecycle.onStart(mockedIntent, 0)
        verify(observer, never()).onEvent(mockedIntent)
    }

    @Test
    fun onStartCommandInvolvingOnAllObservers() {
        fillAllObservers()
        appMetricaServiceLifecycle.onStartCommand(mockedIntent, 0, 0)
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun onDestroyInvolvingOnAllObservers() {
        fillAllObservers()
        appMetricaServiceLifecycle.onDestroy()
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun onConfigurationChanged() {
        fillAllObservers()
        appMetricaServiceLifecycle.onConfigurationChanged(configuration)
        verify(observer, never()).onEvent(any())
    }

    private fun fillAllObservers() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnBindMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnRebindClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnRebindMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnRepeatedBindClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientAction())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun firstClientConnectObserverOnRepeatedBindMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRepeatedBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindMetricaClientActionAfterNonMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindNonMetricaClientActionAfterMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verifyNoInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRepeatedRebindClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun firstClientConnectObserverOnRepeatedRebindMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRepeatedRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindMetricaClientActionAfterNonMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindNonMetricaClientActionAfterMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer, never()).onEvent(intent)
    }

    @Test
    fun firstClientConnectObserverOnBindMetricaClientActionAfterRebindMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindMetricaClientActionAfterBindMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindNonMetricaClientActionAfterRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindNonMetricaClientActionAfterBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindNonMetricaClientActionAfterRebindMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verifyNoInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindMetricaClientActionAfterRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindNonMetricaClientActionAfterBindMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verifyNoInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnRebindMetricaClientActionAfterBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnBindLocationAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun firstClientConnectObserverOnRebindLocationAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun firstClientConnectObserverOnUnbindClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun firstClientConnectObserverOnUnbindMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnUnbindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverOnUnbindLocationAction() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun newClientConnectObserverOnBindClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRebindClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRebindMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedBindClientAction() {
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedBindMetricaClientAction() {
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedBindNonMetricaClientAction() {
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedRebindClientAction() {
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedRebindMetricaClientAction() {
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRepeatedRebindNonMetricaClientAction() {
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer, times(2)).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindMetricaClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer, times(2)).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindNonMetricaClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onBind(intent)
        verify(observer, times(2)).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRebindClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer, times(2)).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnRebindMetricaClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val firstIntent = prepareIntentWithClientActionAndMetricaProcess()
        val secondIntent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onRebind(firstIntent)
        appMetricaServiceLifecycle.onRebind(secondIntent)
        verify(observer).onEvent(firstIntent)
        verify(observer).onEvent(secondIntent)
    }

    @Test
    fun newClientConnectObserverOnRebindNonMetricaClientActionTwice() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onRebind(intent)
        verify(observer, times(2)).onEvent(intent)
    }

    @Test
    fun newClientConnectObserverOnBindLocationAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun newClientConnectObserverOnRebindLocationAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun newClientConnectObserverOnUnbindClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun newClientConnectObserverOnUnbindMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun newClientConnectObserverOnUnbindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun newClientConnectObserverOnUnbindLocationAction() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnBindClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnBindMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnBindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnRebindClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnRebindMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnRebindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnBindLocationAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnRebindLocationAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnUnbindLocationAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION))
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnUnbindClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindNonMetricaClientAction() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllClientsAfterBind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllMetricaClientsAfterBind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllNonMetricaClientsAfterBind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllClientsAfterRebind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllMetricaClientsAfterRebind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnUnbindAllNonMetricaClientsAfterRebind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnPartiallyUnbind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientAction())
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientAction())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction())
        verify(observer, never()).onEvent(any())
    }

    @Test
    fun allClientDisconnectObserverOnPartiallyUnbindMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnPartiallyUnbindNonMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnTotallyUnbind() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientAction()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnTotallyUnbindMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnTotallyUnbindNonMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        appMetricaServiceLifecycle.onRebind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        appMetricaServiceLifecycle.onUnbind(intent)
        verify(observer).onEvent(intent)
    }

    @Test
    fun allClientDisconnectObserverOnTotallyUnbindOnlyMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess())
        appMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun allClientDisconnectObserverOnTotallyUnbindOnlyNonMetricaClients() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        val nonMetricaIntent = prepareIntentWithClientActionAndNonMetricaProcess()
        val metricaIntent = prepareIntentWithClientActionAndMetricaProcess()
        appMetricaServiceLifecycle.onBind(metricaIntent)
        appMetricaServiceLifecycle.onRebind(metricaIntent)
        appMetricaServiceLifecycle.onBind(nonMetricaIntent)
        appMetricaServiceLifecycle.onRebind(nonMetricaIntent)
        appMetricaServiceLifecycle.onUnbind(metricaIntent)
        appMetricaServiceLifecycle.onUnbind(nonMetricaIntent)
        appMetricaServiceLifecycle.onUnbind(nonMetricaIntent)
        verifyNoInteractions(observer)
    }

    @Test
    fun firstClientConnectObserverNotifyingOrder() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer2)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer3)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer4)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer5)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verifyObserversNotifyingOrder(intent)
    }

    @Test
    fun newClientConnectObserverNotifyingOrder() {
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer2)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer3)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer4)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer5)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verifyObserversNotifyingOrder(intent)
    }

    @Test
    fun allClientDisconnectedObserverNotifyingOrder() {
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer)
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer2)
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer3)
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer4)
        appMetricaServiceLifecycle.addAllClientDisconnectedObserver(observer5)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onUnbind(intent)
        verifyObserversNotifyingOrder(intent)
    }

    @Test
    fun clientObserversNotifyingOrder() {
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer2)
        appMetricaServiceLifecycle.addFirstClientConnectObserver(observer3)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer4)
        appMetricaServiceLifecycle.addNewClientConnectObserver(observer5)
        val intent = prepareIntentWithClientActionAndNonMetricaProcess()
        appMetricaServiceLifecycle.onBind(intent)
        verifyObserversNotifyingOrder(intent)
    }

    private fun verifyObserversNotifyingOrder(intent: Intent) {
        val inOrder = inOrder(observer, observer2, observer3, observer4, observer5)
        inOrder.verify(observer).onEvent(intent)
        inOrder.verify(observer2).onEvent(intent)
        inOrder.verify(observer3).onEvent(intent)
        inOrder.verify(observer4).onEvent(intent)
        inOrder.verify(observer5).onEvent(intent)
    }

    private fun intentWithAction(action: String): Intent {
        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(action)
        return intent
    }

    private fun prepareIntentWithClientAction(): Intent {
        return intentWithAction(ACTION_CLIENT_CONNECTION)
    }

    private fun prepareIntentWithClientActionAndMetricaProcess(): Intent {
        return prepareIntentWithWithActionAndPid(ACTION_CLIENT_CONNECTION, Process.myPid())
    }

    private fun prepareIntentWithClientActionAndNonMetricaProcess(): Intent {
        return prepareIntentWithWithActionAndPid(ACTION_CLIENT_CONNECTION, Process.myPid() + 1)
    }

    private fun prepareIntentWithWithActionAndPid(action: String, pid: Int): Intent {
        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(action)
        whenever(intent.data)
            .thenReturn(
                Uri.Builder()
                    .scheme("metrica")
                    .authority("com.yandex.test.package.name")
                    .path("client")
                    .appendQueryParameter("pid", pid.toString())
                    .build()
            )
        return intent
    }

    companion object {
        private const val ACTION_CLIENT_CONNECTION = "io.appmetrica.analytics.IAppMetricaService"
        private const val ACTION_COLLECT_BG_LOCATION = "io.appmetrica.analytics.ACTION_C_BG_L"
    }
}
