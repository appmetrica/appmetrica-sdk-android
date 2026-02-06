package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressLint("UnspecifiedRegisterReceiverFlag")
public class ContextReceiverSafeWrapperTest extends CommonTest {

    @Mock
    private IntentFilter intentFilter;
    @Mock
    private Intent intent;
    @Mock
    private BroadcastReceiver broadcastReceiver;
    @Mock
    private Handler handler;
    @Mock
    private IHandlerExecutor executor;
    private Context context;

    @Rule
    public ContextRule contextRule = new ContextRule();

    private ContextReceiverSafeWrapper contextReceiverSafeWrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(executor.getHandler()).thenReturn(handler);
        context = contextRule.getContext();
        contextReceiverSafeWrapper = new ContextReceiverSafeWrapper(broadcastReceiver);
    }

    @Test
    public void registerReceiverOk() {
        when(context.registerReceiver(broadcastReceiver, intentFilter, null, handler)).thenReturn(intent);
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)).isSameAs(intent);
    }

    @Test
    public void registerReceiverThrows() {
        when(context.registerReceiver(broadcastReceiver, intentFilter, null, handler))
            .thenThrow(new IllegalArgumentException());
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor)).isNull();
    }

    @Test
    public void unregisterReceiverOk() {
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context).unregisterReceiver(broadcastReceiver);
    }

    @Test
    public void unregisterReceiverThrows() {
        doThrow(new IllegalArgumentException()).when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);
        contextReceiverSafeWrapper.unregisterReceiver(context);
    }

    @Test
    public void doNotUnregisterTwice() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenReturn(intent);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);
        verify(context, times(2))
            .registerReceiver(broadcastReceiver, intentFilter, null, handler);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context).unregisterReceiver(broadcastReceiver);
        clearInvocations(context);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verifyNoMoreInteractions(context);
    }

    @Test
    public void doNotUnregisterIfNotRegistered() {
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verifyNoMoreInteractions(context);
    }

    @Test
    public void doNotUnregisterIfRegisterThrew() {
        when(context.registerReceiver(broadcastReceiver, intentFilter, null, handler))
            .thenThrow(new IllegalArgumentException());
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context, never()).unregisterReceiver(broadcastReceiver);
    }

    @Test
    public void tryToUnregisterIfFirstTimeThrew() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenReturn(intent);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter, executor);

        doThrow(new IllegalArgumentException()).when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        doNothing().when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context, times(2)).unregisterReceiver(broadcastReceiver);
    }
}
