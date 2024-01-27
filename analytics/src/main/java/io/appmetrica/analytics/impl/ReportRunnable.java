package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;

class ReportRunnable implements Runnable {

    private static final String TAG = "[ReportRunnable]";

    @NonNull
    private final Context mContext;
    private final CounterReport mReport;
    private final Bundle mExtras;
    @NonNull
    private final ClientRepository mClientRepository;

    ReportRunnable(@NonNull Context context,
                   final CounterReport report,
                   final Bundle extras,
                   @NonNull ClientRepository clientRepository) {
        mContext = context;
        mReport = report;
        mExtras = extras;
        mClientRepository = clientRepository;
    }

    @Override
    public void run() {
        final ClientConfiguration sdkConfig = ClientConfiguration.fromBundle(mContext, mExtras);
        YLogger.d(
                "%sHandle new report with sdkConfig: %s; report = %s",
                TAG,
                sdkConfig,
                mReport
        );
        if (sdkConfig == null) {
            return;
        }

        ClientDescription clientDescription = ClientDescription.fromClientConfiguration(sdkConfig);

        SdkEnvironmentHolder sdkEnvironmentHolder = GlobalServiceLocator.getInstance().getSdkEnvironmentHolder();

        sdkEnvironmentHolder.mayBeUpdateAppVersion(
            sdkConfig.getReporterConfiguration().getAppVersion(),
            sdkConfig.getReporterConfiguration().getAppBuildNumber()
        );
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(sdkConfig.getReporterConfiguration().getDeviceType());

        CommonArguments arguments = new CommonArguments(sdkConfig);
        ClientUnit clientUnit = mClientRepository.getOrCreateClient(clientDescription, arguments);

        clientUnit.handle(mReport, arguments);
    }
}
