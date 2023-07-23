package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class LifecycleDependentComponentManagerTest extends CommonTest {

    @Mock
    private BatteryChargeTypeListener batteryChargeTypeListener;
    @Mock
    private ApplicationStateProviderImpl applicationStateProvider;
    @Mock
    private ServiceLifecycleObserver listener;
    private LifecycleDependentComponentManager lifecycleDependentComponentManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lifecycleDependentComponentManager = new LifecycleDependentComponentManager(
                batteryChargeTypeListener,
                applicationStateProvider
        );
    }

    @Test
    public void getters() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(lifecycleDependentComponentManager.getBatteryChargeTypeListener()).isSameAs(batteryChargeTypeListener);
        softly.assertThat(lifecycleDependentComponentManager.getApplicationStateProvider()).isSameAs(applicationStateProvider);
        softly.assertAll();
    }

    @Test
    public void onCreate() {
        lifecycleDependentComponentManager.addLifecycleObserver(listener);
        lifecycleDependentComponentManager.onCreate();
        verify(batteryChargeTypeListener).onCreate();
        verify(applicationStateProvider).onCreate();
        verify(listener).onCreate();
    }

    @Test
    public void onDestroyed() {
        lifecycleDependentComponentManager.addLifecycleObserver(listener);
        lifecycleDependentComponentManager.onDestroy();
        verify(batteryChargeTypeListener).onDestroy();
        verify(applicationStateProvider).onDestroy();
        verify(listener).onDestroy();
    }
}
