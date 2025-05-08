package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.os.BatteryManager;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeType;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeChangeListener;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BatteryInfoProviderTest extends CommonTest {

    @Mock
    private BatteryChargeTypeListener batteryChargeTypeListener;
    @Mock
    private ChargeTypeChangeListener mChargeTypeChangeListener;
    @Mock
    private ChargeTypeChangeListener mSecondChargeTypeChangeListener;

    private StubbedBlockingExecutor mExecutor;

    @Captor
    private ArgumentCaptor<Consumer<Intent>> listenerCaptor;

    private BatteryInfoProvider mBatteryInfoProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mExecutor = new StubbedBlockingExecutor();

        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(
            100,
            1000,
            BatteryManager.BATTERY_PLUGGED_AC
        ));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
    }

    @Test
    public void testInitialChargeTypeForNullIntent() {
        mockBatteryChangedBroadcastWithIntent(null);
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getChargeType()).isEqualTo(ChargeType.UNKNOWN);
    }

    @Test
    public void testInitialChargeTypeForAc() {
        testInitialChargeType(BatteryManager.BATTERY_PLUGGED_AC, ChargeType.AC);
    }

    @Test
    public void testInitialChargeTypeForUsb() {
        testInitialChargeType(BatteryManager.BATTERY_PLUGGED_USB, ChargeType.USB);
    }

    @Test
    public void testInitialChargeTypeForWireless() {
        testInitialChargeType(
            BatteryManager.BATTERY_PLUGGED_WIRELESS,
            ChargeType.WIRELESS
        );
    }

    @Test
    public void testInitialChargeTypeForNone() {
        testInitialChargeType(-1, ChargeType.NONE);
    }

    @Test
    public void testInitialChargeTypeForUnknown() {
        testInitialChargeType(1000, ChargeType.NONE);
    }

    @Test
    public void testInitialChargeTypeForNull() {
        testInitialChargeType(null, ChargeType.NONE);
    }

    private void testInitialChargeType(Integer plugged, ChargeType expectedValue) {
        Intent batteryInfoIntent = prepareIntentWithBatteryInfo(10, 100, plugged);
        mockBatteryChangedBroadcastWithIntent(batteryInfoIntent);
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getChargeType()).isEqualTo(expectedValue);
    }

    @Test
    public void testGetBatteryLevel() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(100, 1000, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isEqualTo(10);
    }

    @Test
    public void testGetBatteryLevelForNullIntent() {
        mockBatteryChangedBroadcastWithIntent(null);
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testGetBatteryLevelForMissingValues() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(null, null, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testGetBatteryLevelForNegativeSourceBatteryLevel() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(-100, 1000, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testGetBatteryLevelForNegativeScale() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(100, -1000, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testGetBatteryLevelForZeroSourceBatteryLevel() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(0, 100, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testGetBatteryLevelForZeroScale() {
        mockBatteryChangedBroadcastWithIntent(prepareIntentWithBatteryInfo(100, 0, 1));
        mBatteryInfoProvider = new BatteryInfoProvider(mExecutor, batteryChargeTypeListener);
        assertThat(mBatteryInfoProvider.getBatteryLevel()).isNull();
    }

    @Test
    public void testRegisterNotifyListenersImmediately() {
        mBatteryInfoProvider.registerChargeTypeListener(mChargeTypeChangeListener);
        verify(mChargeTypeChangeListener).onChargeTypeChanged(ChargeType.AC);
        mBatteryInfoProvider.registerChargeTypeListener(mSecondChargeTypeChangeListener);
        verify(mSecondChargeTypeChangeListener).onChargeTypeChanged(ChargeType.AC);
    }

    @Test
    public void testDispatchPowerStateChangeToAllListenersForAc() {
        testDispatchPowerStateChangeToAllListeners(
            BatteryManager.BATTERY_PLUGGED_AC,
            ChargeType.AC
        );
    }

    @Test
    public void testDispatchPowerStateChangeToAllListenersForUsb() {
        testDispatchPowerStateChangeToAllListeners(
            BatteryManager.BATTERY_PLUGGED_USB,
            ChargeType.USB
        );
    }

    @Test
    public void testDispatchPowerStateChangeToAllListenersForWireless() {
        testDispatchPowerStateChangeToAllListeners(
            BatteryManager.BATTERY_PLUGGED_WIRELESS,
            ChargeType.WIRELESS
        );
    }

    @Test
    public void testDispatchPowerStateChangeToAllListenersForMissing() {
        testDispatchPowerStateChangeToAllListeners(
            null,
            ChargeType.NONE
        );
    }

    @Test
    public void testDispatchPowerStateChangeToAllListenersForUnknown() {
        testDispatchPowerStateChangeToAllListeners(
            -1,
            ChargeType.NONE
        );
    }

    private void testDispatchPowerStateChangeToAllListeners(Integer plugged,
                                                            ChargeType chargeType) {
        InOrder inOrder = inOrder(mChargeTypeChangeListener, mSecondChargeTypeChangeListener);

        mBatteryInfoProvider.registerChargeTypeListener(mChargeTypeChangeListener);
        mBatteryInfoProvider.registerChargeTypeListener(mSecondChargeTypeChangeListener);

        inOrder.verify(mChargeTypeChangeListener)
            .onChargeTypeChanged(ChargeType.AC);
        inOrder.verify(mSecondChargeTypeChangeListener)
            .onChargeTypeChanged(ChargeType.AC);

        Intent intent = prepareIntentWithBatteryInfo(100, 1000, plugged);
        Consumer<Intent> listener = interceptBatteryChangedListener();
        listener.consume(intent);

        inOrder.verify(
            mChargeTypeChangeListener,
            times(chargeType == ChargeType.AC ? 0 : 1)
        ).onChargeTypeChanged(chargeType);
        inOrder.verify(
            mSecondChargeTypeChangeListener,
            times(chargeType == ChargeType.AC ? 0 : 1)
        ).onChargeTypeChanged(chargeType);
        inOrder.verifyNoMoreInteractions();
    }

    private void mockBatteryChangedBroadcastWithIntent(Intent intent) {
        when(batteryChargeTypeListener.addStickyBatteryChargeTypeListener(any(Consumer.class))).thenReturn(intent);
    }

    private Consumer<Intent> interceptBatteryChangedListener() {
        verify(batteryChargeTypeListener).addStickyBatteryChargeTypeListener(listenerCaptor.capture());
        return listenerCaptor.getValue();
    }

    private Intent prepareIntentWithBatteryInfo(Integer batteryLevel, Integer batteryScale, Integer plugged) {
        Intent intent = new Intent();

        if (batteryLevel != null) {
            intent.putExtra(BatteryManager.EXTRA_LEVEL, batteryLevel);
        }

        if (batteryScale != null) {
            intent.putExtra(BatteryManager.EXTRA_SCALE, batteryScale);
        }

        if (plugged != null) {
            intent.putExtra(BatteryManager.EXTRA_PLUGGED, plugged);
        }

        return intent;
    }

}
