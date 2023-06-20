package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppMetricaServiceLifecycle implements MetricaServiceLifecycleCallback {

    public interface LifecycleObserver {
        void onEvent(@NonNull Intent intent);
    }

    interface Condition {
        boolean match(@NonNull Intent intent, @NonNull AppMetricaServiceLifecycle lifecycle);
    }

    private static final String TAG = "[MetricaServiceLifecycle]";

    private final HashMultimap<String, Integer> mBoundProcesses = new HashMultimap<String, Integer>();

    private final Map<LifecycleObserver, Condition> mConnectObservers =
            new LinkedHashMap<LifecycleObserver, Condition>();
    private final Map<LifecycleObserver, Condition> mDisconnectObservers =
            new LinkedHashMap<LifecycleObserver, Condition>();

    @Override
    public void onCreate() {
        //Do nothing
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        //Do nothing
    }

    @Override
    public void onStartCommand(final Intent intent, final int flags, final int startId) {
        //Do nothing
    }

    @Override
    public void onBind(final Intent intent) {
        if (intent != null) {
            handleBindOrRebind(intent);
        }
    }

    @Override
    public void onRebind(final Intent intent) {
        if (intent != null) {
            handleBindOrRebind(intent);
        }
    }

    private void handleBindOrRebind(@NonNull final Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) == false) {
            mBoundProcesses.put(action, getPid(intent));
            YLogger.d(
                    "%sonBindOrRebind with action = %s. Is metrica process: %b. Current bound clients: %s",
                    TAG,
                    action,
                    isMetricaProcess(getPid(intent)),
                    mBoundProcesses
            );
        }
        notifyObservers(intent, mConnectObservers);
    }

    private void notifyObservers(@NonNull Intent intent, @NonNull Map<LifecycleObserver, Condition> observers) {
        for (Map.Entry<LifecycleObserver, Condition> observerEntry : observers.entrySet()) {
            if (observerEntry.getValue().match(intent, this)) {
                observerEntry.getKey().onEvent(intent);
            }
        }
    }

    @Override
    public void onUnbind(final Intent intent) {
        if (intent != null) {
            handleUnbind(intent);
        }
    }

    private void handleUnbind(@NonNull final Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) == false) {
            mBoundProcesses.remove(action, getPid(intent));
            YLogger.d(
                    "%sonUnbind with action = %s. Is metrica process: %b. Current bound clients after remove: %s",
                    TAG,
                    action,
                    isMetricaProcess(getPid(intent)),
                    mBoundProcesses
            );
        }
        notifyObservers(intent, mDisconnectObservers);
    }

    @Override
    public void onDestroy() {
        //Do nothing
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Do nothing
    }

    public void addFirstClientConnectObserver(@NonNull LifecycleObserver lifecycleObserver) {
        mConnectObservers.put(lifecycleObserver, new Condition() {
            @Override
            public boolean match(@NonNull final Intent intent, @NonNull final AppMetricaServiceLifecycle lifecycle) {
                return isFirstNonMetricaClientAction(intent);
            }
        });
    }

    public void addNewClientConnectObserver(@NonNull LifecycleObserver lifecycleObserver) {
        mConnectObservers.put(lifecycleObserver, new Condition() {
            @Override
            public boolean match(@NonNull final Intent intent, @NonNull final AppMetricaServiceLifecycle lifecycle) {
                return isNonMetricaClientAction(intent);
            }
        });
    }

    public void addAllClientDisconnectedObserver(@NonNull LifecycleObserver lifecycleObserver) {
        mDisconnectObservers.put(lifecycleObserver, new Condition() {
            @Override
            public boolean match(@NonNull final Intent intent, @NonNull final AppMetricaServiceLifecycle lifecycle) {
                return isNonMetricaClientAction(intent) && noMoreNonMetricaBoundClients();
            }
        });
    }

    private boolean isFirstNonMetricaClientAction(@NonNull Intent intent) {
        return isNonMetricaClientAction(intent) && hasSingleBoundNonMetricaClient();
    }

    private boolean isClientAction(@Nullable String action) {
        return AppMetricaServiceAction.ACTION_CLIENT_CONNECTION.equals(action);
    }

    private boolean isNonMetricaClientAction(@NonNull Intent intent) {
        String action = intent.getAction();
        if (isClientAction(action)) {
            return isMetricaProcess(getPid(intent)) == false;
        }

        return false;
    }

    private boolean hasSingleBoundNonMetricaClient() {
        return getNonMetricaClientsCount() == 1;
    }

    private int getNonMetricaClientsCount() {
        int nonMetricaClientCount = 0;
        Collection<Integer> pids = getClientPids();
        if (Utils.isNullOrEmpty(pids) == false) {
            for (int pid : pids) {
                if (isMetricaProcess(pid) == false) {
                    nonMetricaClientCount ++;
                }
            }
        }
        return nonMetricaClientCount;
    }

    private boolean noMoreNonMetricaBoundClients() {
        return getNonMetricaClientsCount() == 0;
    }

    private Collection<Integer> getClientPids() {
        return mBoundProcesses.get(AppMetricaServiceAction.ACTION_CLIENT_CONNECTION);
    }

    private boolean isMetricaProcess(int pid) {
        return pid == Process.myPid();
    }

    private int getPid(@NonNull Intent intent) {
        int pid = -1;
        Uri intentData = intent.getData();
        if (intentData != null && intentData.getPath().equals("/" + ServiceUtils.PATH_CLIENT)) {
            try {
                pid = Integer.parseInt(intentData.getQueryParameter(ServiceUtils.PARAMETER_PID));
            } catch (Throwable e) {
                YLogger.e(e, "%s:%s", TAG, e.getMessage());
            }
        }
        return pid;
    }
}
