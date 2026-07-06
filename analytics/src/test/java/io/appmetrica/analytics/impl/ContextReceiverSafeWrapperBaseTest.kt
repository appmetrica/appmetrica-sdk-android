package io.appmetrica.analytics.impl

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.androidtestutils.rules.ContextRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal abstract class ContextReceiverSafeWrapperBaseTest : CommonTest() {

    protected val handler = mock<Handler>()
    protected val broadcastReceiver = mock<BroadcastReceiver>()
    protected val intentFilter = mock<IntentFilter>()
    protected val intent = mock<Intent>()
    protected val executor = mock<IHandlerExecutor>()

    @get:Rule
    val contextRule = ContextRule()
    protected val context by contextRule

    protected lateinit var contextReceiverSafeWrapper: ContextReceiverSafeWrapper

    @Before
    fun setUp() {
        whenever(executor.handler).thenReturn(handler)
        contextReceiverSafeWrapper = ContextReceiverSafeWrapper(broadcastReceiver)
    }

    protected abstract fun stubRegisterReceiverReturns(result: Intent)
    protected abstract fun stubRegisterReceiverThrows(throwable: Throwable)
    protected abstract fun verifyRegisterReceiver(times: Int)

    @Test
    fun registerReceiverOk() {
        stubRegisterReceiverReturns(intent)
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)).isSameAs(intent)
    }

    @Test
    fun registerReceiverThrows() {
        stubRegisterReceiverThrows(IllegalArgumentException())
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)).isNull()
    }

    @Test
    fun unregisterReceiverOk() {
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verify(context).unregisterReceiver(broadcastReceiver)
    }

    @Test
    fun unregisterReceiverThrows() {
        doThrow(IllegalArgumentException()).whenever(context).unregisterReceiver(broadcastReceiver)
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)
        contextReceiverSafeWrapper.unregisterReceiver(context)
    }

    @Test
    fun doNotUnregisterTwice() {
        stubRegisterReceiverReturns(intent)
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)
        verifyRegisterReceiver(2)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verify(context).unregisterReceiver(broadcastReceiver)
        clearInvocations(context)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verifyNoMoreInteractions(context)
    }

    @Test
    fun doNotUnregisterIfNotRegistered() {
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verifyNoMoreInteractions(context)
    }

    @Test
    fun doNotUnregisterIfRegisterThrew() {
        stubRegisterReceiverThrows(IllegalArgumentException())
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verify(context, never()).unregisterReceiver(broadcastReceiver)
    }

    @Test
    fun tryToUnregisterIfFirstTimeThrew() {
        stubRegisterReceiverReturns(intent)
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)

        doThrow(IllegalArgumentException()).whenever(context).unregisterReceiver(broadcastReceiver)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        doNothing().whenever(context).unregisterReceiver(broadcastReceiver)
        contextReceiverSafeWrapper.unregisterReceiver(context)
        verify(context, times(2)).unregisterReceiver(broadcastReceiver)
    }
}
