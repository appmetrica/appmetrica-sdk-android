package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;

public abstract class AbstractClientUnit implements ClientUnit {

    @NonNull private final Context mContext;
    @NonNull private final RegularDispatcherComponent mComponentUnit;

    public AbstractClientUnit(@NonNull Context context,
                              @NonNull RegularDispatcherComponent componentUnit) {
        mContext = context.getApplicationContext();
        mComponentUnit = componentUnit;
        mComponentUnit.connectClient(this);
        GlobalServiceLocator.getInstance().getLocationClientApi().registerWakelock(this);
    }

    @Override
    public void handle(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
        handleReport(report, sdkConfig);
    }

    protected abstract void handleReport(@NonNull CounterReport report,
                                         @NonNull CommonArguments sdkConfig);

    public void onDisconnect() {
        mComponentUnit.disconnectClient(this);
        GlobalServiceLocator.getInstance().getLocationClientApi().removeWakelock(this);
    }

    @NonNull
    public RegularDispatcherComponent getComponentUnit() {
        return mComponentUnit;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

}
