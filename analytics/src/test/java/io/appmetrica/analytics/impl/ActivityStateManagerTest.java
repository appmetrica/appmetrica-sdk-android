package io.appmetrica.analytics.impl;

import android.app.Activity;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityStateManagerTest extends CommonTest {

    @Mock
    private Activity firstActivity;
    @Mock
    private Activity secondActivity;
    private ActivityStateManager activityStateManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        activityStateManager = new ActivityStateManager();
    }

    @Test
    public void stateChangedForNullActivity() {
        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).isTrue();

        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED)).isTrue();
        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED)).isTrue();

        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).isTrue();
    }

    @Test
    public void stateChangedForNonNullActivities() {
        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.RESUMED)).isFalse();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.RESUMED)).isFalse();

        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.PAUSED)).isTrue();
        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.PAUSED)).isFalse();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.PAUSED)).isTrue();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.PAUSED)).isFalse();

        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(firstActivity, ActivityStateManager.ActivityState.RESUMED)).isFalse();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.RESUMED)).isTrue();
        assertThat(activityStateManager.didStateChange(secondActivity, ActivityStateManager.ActivityState.RESUMED)).isFalse();
    }
}
