package io.appmetrica.analytics.impl.network;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy;
import io.appmetrica.analytics.impl.PhoneUtils;
import io.appmetrica.analytics.impl.utils.ConnectionTypeProviderImpl;
import io.appmetrica.analytics.impl.utils.IConnectionTypeProvider;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.EnumSet;

public class ExecutionPolicyBasedOnConnection implements IExecutionPolicy {

    private static final String TAG = "[ExecutionPolicyBasedOnConnection]";

    private static final EnumSet<PhoneUtils.NetworkType> FORBIDDEN_NETWORK_TYPES = EnumSet.of(
            PhoneUtils.NetworkType.OFFLINE
    );

    private IConnectionTypeProvider mConnectionTypeProvider = new ConnectionTypeProviderImpl();
    private final Context mContext;

    public ExecutionPolicyBasedOnConnection(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public boolean canBeExecuted() {
        PhoneUtils.NetworkType connectionType = mConnectionTypeProvider.getConnectionType(mContext);
        boolean canBeExecuted = FORBIDDEN_NETWORK_TYPES.contains(connectionType) == false;
        DebugLogger.info(
            TAG,
            "can request executed on network with type %s? %b",
            connectionType.toString(),
            canBeExecuted
        );
        return canBeExecuted;
    }

}
