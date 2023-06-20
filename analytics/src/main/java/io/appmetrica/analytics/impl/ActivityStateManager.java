package io.appmetrica.analytics.impl;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import java.util.WeakHashMap;

public class ActivityStateManager {

    public enum ActivityState {
        UNDEFINED, RESUMED, PAUSED
    }

    @NonNull
    private final WeakHashMap<Activity, ActivityState> activityStates = new WeakHashMap<Activity, ActivityState>();

    @ApiProxyThread
    public boolean didStateChange(@Nullable Activity activity, @NonNull ActivityState newState) {
        if (activity == null || activityStates.get(activity) != newState) {
            if (activity != null) {
                activityStates.put(activity, newState);
            }
            return true;
        }
        return false;
    }
}
