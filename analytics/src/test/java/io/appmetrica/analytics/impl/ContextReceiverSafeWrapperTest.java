package io.appmetrica.analytics.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ContextReceiverSafeWrapperTest extends CommonTest {

    @Mock
    private IntentFilter intentFilter;
    @Mock
    private Intent intent;
    @Mock
    private BroadcastReceiver broadcastReceiver;
    private Context context;
    private ContextReceiverSafeWrapper contextReceiverSafeWrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        contextReceiverSafeWrapper = new ContextReceiverSafeWrapper(broadcastReceiver);
    }

    @Test
    public void registerReceiverOk() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenReturn(intent);
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter)).isSameAs(intent);
    }

    @Test
    public void registerReceiverThrows() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenThrow(new IllegalArgumentException());
        assertThat(contextReceiverSafeWrapper.registerReceiver(context, intentFilter)).isNull();
    }

    @Test
    public void unregisterReceiverOk() {
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context).unregisterReceiver(broadcastReceiver);
    }

    @Test
    public void unregisterReceiverThrows() {
        doThrow(new IllegalArgumentException()).when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
        contextReceiverSafeWrapper.unregisterReceiver(context);
    }

    @Test
    public void doNotUnregisterTwice() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenReturn(intent);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
        verify(context, times(2)).registerReceiver(broadcastReceiver, intentFilter);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context).unregisterReceiver(broadcastReceiver);
        clearInvocations(context);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verifyZeroInteractions(context);
    }

    @Test
    public void doNotUnregisterIfNotRegistered() {
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verifyZeroInteractions(context);
    }

    @Test
    public void doNotUnregisterIfRegisterThrew() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenThrow(new IllegalArgumentException());
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context, never()).unregisterReceiver(broadcastReceiver);
    }

    @Test
    public void tryToUnregisterIfFirstTimeThrew() {
        when(context.registerReceiver(broadcastReceiver, intentFilter)).thenReturn(intent);
        contextReceiverSafeWrapper.registerReceiver(context, intentFilter);

        doThrow(new IllegalArgumentException()).when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        doNothing().when(context).unregisterReceiver(broadcastReceiver);
        contextReceiverSafeWrapper.unregisterReceiver(context);
        verify(context, times(2)).unregisterReceiver(broadcastReceiver);
    }
}
