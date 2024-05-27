package io.appmetrica.analytics.impl.telephony;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;

public class TelephonyDataProvider {

    private static final String TAG = "[TelephonyDataProvider]";

    @NonNull
    private final TelephonyInfoAdapterApplier<List<SimInfo>> simInfoAdapterApplier;
    @NonNull
    private final TelephonyInfoAdapterApplier<MobileConnectionDescription> mobileConnectionDescriptionAdapter;

    public TelephonyDataProvider(@NonNull Context context) {
        if (new SafePackageManager().hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
            DebugLogger.INSTANCE.info(TAG, "Use real applier");
            simInfoAdapterApplier = new BaseTelephonyInfoAdapterApplier<>(
                new SimInfoExtractor(context)
            );
            mobileConnectionDescriptionAdapter = new BaseTelephonyInfoAdapterApplier<>(
                new MobileConnectionDescriptionExtractor(context)
            );
        } else {
            DebugLogger.INSTANCE.warning(
                TAG,
                "Feature 'android.hardware.telephony' is missing. So use stubs"
            );
            simInfoAdapterApplier = new DummyTelephonyInfoAdapterApplier<>();
            mobileConnectionDescriptionAdapter = new DummyTelephonyInfoAdapterApplier<>();
        }
    }

    public synchronized void adoptSimInfo(@NonNull TelephonyInfoAdapter<List<SimInfo>> adapter) {
        DebugLogger.INSTANCE.info(TAG, "Apply sim info adapter");
        simInfoAdapterApplier.applyAdapter(adapter);
    }

    public synchronized void adoptMobileConnectionDescription(
        @NonNull TelephonyInfoAdapter<MobileConnectionDescription> adapter) {
        DebugLogger.INSTANCE.info(TAG, "Apply mobile connection adapter");
        mobileConnectionDescriptionAdapter.applyAdapter(adapter);
    }
}
