package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class StartupCenter {

    private static final String TAG = "[StartupCenter]";

    public static final StartupCenter getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final HashMultimap<String, StartupListener> mStartupListenersByPackage =
            new HashMultimap<String, StartupListener>();
    private final HashMap<String, StartupUnit> mStartupUnitsByPackage =
            new HashMap<String, StartupUnit>();
    private StartupState mLastStartupState = null;

    private final StartupResultListener mResultListener = new StartupResultListener() {
        public void onStartupChanged(@NonNull String packageName, @NonNull StartupState newState) {
            final List<StartupListener> listeners;
            synchronized (mStartupUnitsByPackage) {
                mLastStartupState = newState;
                listeners = getStartupListeners(packageName);
            }
            YLogger.debug(TAG,"startup changed for package %s. Total listeners: %d. New value %s",
                packageName, listeners.size(), newState);
            for (StartupListener listener : listeners) {
                listener.onStartupChanged(newState);
            }
        }

        public void onStartupError(@NonNull String packageName,
                                   @NonNull StartupError error,
                                   @Nullable StartupState existingState) {
            YLogger.debug(TAG, "startup failed for package %s. reason %s", packageName, error);
            final List<StartupListener> listeners;
            synchronized (mStartupUnitsByPackage) {
                listeners = getStartupListeners(packageName);
            }
            for (StartupListener listener : listeners) {
                YLogger.debug(TAG, "Notify listener %s with new startup error", listener);
                listener.onStartupError(error, existingState);
            }
        }

        @NonNull
        public List<StartupListener> getStartupListeners(@NonNull String packageName) {
            Collection<StartupListener> startupListeners = mStartupListenersByPackage.get(packageName);
            if (startupListeners == null) {
                YLogger.debug(TAG, "no listeners found");
                return new ArrayList<StartupListener>();
            } else {
                YLogger.debug(TAG, "%d listeners found", startupListeners.size());
                return new ArrayList<StartupListener>(startupListeners);
            }
        }
    };

    public StartupUnit getOrCreateStartupUnit(@NonNull Context context,
                                              @NonNull ComponentId componentId,
                                              @NonNull StartupRequestConfig.Arguments arguments) {
        StartupUnit unit;
        boolean needToUpdateConfiguration = true;
        YLogger.debug(TAG, "getOrCreateStartupUnit for component %s", componentId);
        unit = mStartupUnitsByPackage.get(componentId.getPackage());
        if (unit == null) {
            synchronized (mStartupUnitsByPackage) {
                unit = mStartupUnitsByPackage.get(componentId.getPackage());
                if (unit == null) {
                    needToUpdateConfiguration = false;
                    unit = createStartupUnit(context, componentId, arguments);
                    mStartupUnitsByPackage.put(componentId.getPackage(), unit);
                }
            }
        }
        if (needToUpdateConfiguration) {
            unit.updateConfiguration(arguments);
        }
        return unit;
    }

    @VisibleForTesting
    StartupUnit createStartupUnit(@NonNull Context context,
                                  @NonNull ComponentId componentId,
                                  @NonNull StartupRequestConfig.Arguments arguments) {
        YLogger.debug(TAG, "createStartupUnit for component %s", componentId);
        StartupUnit startupUnit = new StartupUnit(
            new StartupUnitComponents(
                context,
                componentId.getPackage(),
                arguments,
                mResultListener
            )
        );
        startupUnit.init();
        return startupUnit;
    }

    public void registerStartupListener(@NonNull ComponentId componentId,
                                        @NonNull StartupListener startupListener) {
        synchronized (mStartupUnitsByPackage) {
            YLogger.debug(
                TAG,
                "registerStartupListener: %s for component %s",
                startupListener,
                componentId
            );
            mStartupListenersByPackage.put(componentId.getPackage(), startupListener);
            if (mLastStartupState != null) {
                startupListener.onStartupChanged(mLastStartupState);
            }
        }
    }

    public void unregisterStartupListener(@NonNull ComponentId componentId, StartupListener startupListener) {
        Collection<StartupListener> remainingUnits;
        YLogger.debug(
            TAG,
            "unregisterStartupListeners %s for component %s",
            startupListener,
            componentId
        );
        synchronized (mStartupUnitsByPackage) {
            remainingUnits = mStartupListenersByPackage.remove(
                    componentId.getPackage(),
                    startupListener
            );
        }
        if (Utils.isNullOrEmpty(remainingUnits)) {
            mStartupUnitsByPackage.remove(componentId.getPackage());
        }
    }

    /*
        Order:
        1. Main
        2. Blank
        NO other. It's better to send with blank client and it's config, but not with other's config.
        And do not forget about persistent things like clids and etc.
     */

    private static final class InstanceHolder {
        static final StartupCenter INSTANCE = new StartupCenter();
    }

    @VisibleForTesting
    Collection<StartupListener> getListeners(String packageName) {
        return mStartupListenersByPackage.get(packageName);
    }

    @VisibleForTesting
    StartupUnit getStartupUnit(String packageName) {
        return mStartupUnitsByPackage.get(packageName);
    }

    @VisibleForTesting
    StartupResultListener getResultListener() {
        return mResultListener;
    }

}
