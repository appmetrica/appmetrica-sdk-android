package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.utils.BackgroundBroadcastReceiver;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BatteryChargeTypeListenerTest extends CommonTest {

    private final ICommonExecutor executor = new StubbedBlockingExecutor();
    @Mock
    private Intent initialIntent;
    @Mock
    private Consumer<Intent> firstListener;
    @Mock
    private Consumer<Intent> secondListener;
    @Mock
    private ContextReceiverSafeWrapper contextReceiverSafeWrapper;
    @Mock
    private ContextReceiverSafeWrapper.Provider contextReceiverSafeWrapperProvider;
    @Captor
    private ArgumentCaptor<BackgroundBroadcastReceiver> receiverCaptor;
    private Context context;
    private BatteryChargeTypeListener batteryChargeTypeListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(contextReceiverSafeWrapperProvider.create(any(BackgroundBroadcastReceiver.class))).thenReturn(contextReceiverSafeWrapper);
        when(contextReceiverSafeWrapper.registerReceiver(
            any(Context.class),
            any(IntentFilter.class)
        )).thenReturn(initialIntent);
        batteryChargeTypeListener = new BatteryChargeTypeListener(context, executor, contextReceiverSafeWrapperProvider);
    }

    @Test
    public void onCreatedNoListeners() {
        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), argThat(new ArgumentMatcher<IntentFilter>() {
            @Override
            public boolean matches(IntentFilter argument) {
                return argument.hasAction(Intent.ACTION_BATTERY_CHANGED);
            }
        }));

        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener)).isSameAs(initialIntent);
        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(secondListener)).isSameAs(initialIntent);
    }

    @Test
    public void onCreatedHasListeners() {
        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener)).isNull();
        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(secondListener)).isNull();

        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), argThat(new ArgumentMatcher<IntentFilter>() {
            @Override
            public boolean matches(IntentFilter argument) {
                return argument.hasAction(Intent.ACTION_BATTERY_CHANGED);
            }
        }));

        verify(firstListener).consume(initialIntent);
        verify(secondListener).consume(initialIntent);
    }

    @Test
    public void onDestroyedNoListeners() {
        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), any(IntentFilter.class));

        batteryChargeTypeListener.onDestroy();
        verify(contextReceiverSafeWrapper).unregisterReceiver(context);

        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener)).isNull();
        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(secondListener)).isNull();
    }

    @Test
    public void onDestroyedHasListeners() {
        batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener);
        batteryChargeTypeListener.addStickyBatteryChargeTypeListener(secondListener);

        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), any(IntentFilter.class));

        clearInvocations(firstListener, secondListener);
        batteryChargeTypeListener.onDestroy();
        verify(contextReceiverSafeWrapper).unregisterReceiver(context);

        verify(firstListener).consume(null);
        verify(secondListener).consume(null);
    }

    @Test
    public void statusChanged() {
        Intent newIntent = mock(Intent.class);
        verify(contextReceiverSafeWrapperProvider).create(receiverCaptor.capture());
        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), any(IntentFilter.class));
        batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener);

        receiverCaptor.getValue().onReceive(context, newIntent);
        verify(firstListener).consume(newIntent);
        assertThat(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(secondListener)).isSameAs(newIntent);
    }

    @Test
    public void twiceOnCreateAndOnDestroy() {
        batteryChargeTypeListener.addStickyBatteryChargeTypeListener(firstListener);

        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), argThat(new ArgumentMatcher<IntentFilter>() {
            @Override
            public boolean matches(IntentFilter argument) {
                return argument.hasAction(Intent.ACTION_BATTERY_CHANGED);
            }
        }));

        verify(firstListener).consume(initialIntent);

        clearInvocations(contextReceiverSafeWrapper, firstListener);
        batteryChargeTypeListener.onCreate();
        verify(contextReceiverSafeWrapper).registerReceiver(same(context), any(IntentFilter.class));
        verify(firstListener).consume(initialIntent);

        batteryChargeTypeListener.onDestroy();
        verify(contextReceiverSafeWrapper).unregisterReceiver(context);
        verify(firstListener).consume(null);

        clearInvocations(contextReceiverSafeWrapper, firstListener);
        batteryChargeTypeListener.onDestroy();
        verify(contextReceiverSafeWrapper).unregisterReceiver(context);
        verify(firstListener).consume(null);
    }
}
