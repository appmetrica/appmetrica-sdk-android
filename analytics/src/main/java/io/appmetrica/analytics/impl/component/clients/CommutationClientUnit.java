package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ClientIdentifiersChangedListener;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.logger.internal.YLogger;

/*
 * This class main purpose is to bind client process with service process.
 * It's completely different from other clients. The only thing they have in common - an IPC protocol, based on
 * main service interface and its reportData method. One method using in two situations:
 * when somebody sends event and when SDK's client need something from service. The decision what to do next
 * is made based on special flag in CounterConfiguration (isCommutation()).
 * EventsManager.InternalEvents, that handled by CommutationComponentUnit are never handled by
 * ComponentUnit and vice versa.
 */
public class CommutationClientUnit implements ClientIdentifiersChangedListener, ClientUnit {

    private static final String TAG = "[CommutationClientUnit]";

    @NonNull
    private final Context mContext;
    @NonNull
    private CommutationDispatcherComponent mComponentUnit;
    @Nullable
    private final ResultReceiver mResultReceiver;

    public CommutationClientUnit(@NonNull Context context,
                                 @NonNull CommutationDispatcherComponent componentUnit,
                                 @NonNull CommonArguments clientConfiguration) {
        mContext = context;
        mComponentUnit = componentUnit;
        mResultReceiver = clientConfiguration.dataResultReceiver;
        mComponentUnit.connectClient(this);
    }

    @Override
    public void handle(@NonNull CounterReport counterReport, @NonNull CommonArguments sdkConfig) {
        mComponentUnit.updateSdkConfig(sdkConfig.componentArguments);
        mComponentUnit.handleReport(counterReport, this);
    }

    @Override
    public void onClientIdentifiersChanged(@NonNull ClientIdentifiersHolder clientIdentifiersHolder) {
        YLogger.debug(TAG, "onClientIdentifiersChanged. Receiver %s", mResultReceiver);
        DataResultReceiver.notifyOnStartupUpdated(mResultReceiver, clientIdentifiersHolder);
    }

    @Override
    public void onDisconnect() {
        mComponentUnit.disconnectClient(this);
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public CommutationDispatcherComponent getComponent() {
        return mComponentUnit;
    }

    @NonNull
    @VisibleForTesting
    public ResultReceiver getResultReceiver() {
        return mResultReceiver;
    }
}
