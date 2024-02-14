package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.service.ServiceDataReporter;
import io.appmetrica.analytics.impl.service.commands.ReportToServiceCallable;
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.TimeUnit;

public class ReportsSender implements ServiceDataReporter {

    private static final String TAG = "[ReportsSender]";

    private final AppMetricaConnector mServiceConnector;
    private final ServiceCallableFactory serviceCallableFactory;

    private final ICommonExecutor mExecutor;

    public ReportsSender(
        @NonNull final AppMetricaConnector appMetricaConnector,
        @NonNull final ServiceCallableFactory serviceCallableFactory
    ) {
        this(
            appMetricaConnector,
            serviceCallableFactory,
            ClientServiceLocator.getInstance().getClientExecutorProvider().getReportSenderExecutor()
        );
    }

    @VisibleForTesting
    ReportsSender(
        @NonNull final AppMetricaConnector appMetricaConnector,
        @NonNull final ServiceCallableFactory serviceCallableFactory,
        @NonNull final ICommonExecutor executor
    ) {
        mExecutor = executor;
        this.serviceCallableFactory = serviceCallableFactory;
        mServiceConnector = appMetricaConnector;
    }

    public void queueReport(ReportToSend report) {
        mExecutor.submit(report.isCrashReport() ?
            serviceCallableFactory.createCrashCallable(report) :
            serviceCallableFactory.createReportCallable(report)
        );
    }

    public void sendCrash(@NonNull ReportToSend report) {
        ReportToServiceCallable callable = serviceCallableFactory.createCrashCallable(report);
        if (mServiceConnector.isConnected()) {
            try {
                mExecutor.submit(callable).get(4, TimeUnit.SECONDS);
            } catch (Throwable e) {
                YLogger.error(TAG, e);
            }
        }
        // flag will be false if it is still executing but check inside CrashCallable will prevent duplicates
        if (!callable.isExecuted()) {
            try {
                callable.call();
            } catch (Throwable ex) {
                YLogger.error(TAG, ex);
            }
        }
    }

    @Override
    public void reportData(int type, @NonNull Bundle bundle) {
        mExecutor.submit(serviceCallableFactory.createTypedReportCallable(type, bundle));
    }

    public void queueResumeUserSession(@NonNull final ProcessConfiguration processConfiguration) {
        mExecutor.submit(serviceCallableFactory.createResumeUseSessionCallable(processConfiguration));
    }

    public void queuePauseUserSession(@NonNull final ProcessConfiguration processConfiguration) {
        mExecutor.submit(serviceCallableFactory.createPauseUseSessionCallable(processConfiguration));
    }
}
