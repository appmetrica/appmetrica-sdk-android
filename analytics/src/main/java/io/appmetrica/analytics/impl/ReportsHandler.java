package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.UnhandledExceptionEventFormer;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProtoSerializable;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter;
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory;
import io.appmetrica.analytics.impl.startup.StartupIdentifiersProvider;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import java.util.List;
import java.util.Map;

public class ReportsHandler {

    private static final String TAG = "[ReportsHandler]";

    // Create anonymous environment to be able to send some events (like startup) without apiKey
    @NonNull
    private final CommutationReporterEnvironment mCommutationReportEnvironment;
    @NonNull
    private final UnhandledExceptionEventFormer mEventFormer;
    private final ReportsSender mReportsSender;
    private final AppMetricaConnector mConnector;
    private StartupIdentifiersProvider mStartupParamsProvider;
    @NonNull
    private final ServiceCallableFactory serviceCallableFactory;

    ReportsHandler(
        @NonNull final ProcessConfiguration processConfiguration,
        @NonNull final Context context,
        @NonNull final ICommonExecutor connectorExecutor
    ) {
        this(
            context,
            new AppMetricaConnector(context, connectorExecutor),
            new UnhandledExceptionEventFormer(),
            new CommutationReporterEnvironment(processConfiguration)
        );
    }

    private ReportsHandler(
        @NonNull final Context context,
        @NonNull final AppMetricaConnector appMetricaConnector,
        @NonNull final UnhandledExceptionEventFormer eventFormer,
        @NonNull final CommutationReporterEnvironment commutationReporterEnvironment
    ) {
        this(
            appMetricaConnector,
            eventFormer,
            commutationReporterEnvironment,
            new ServiceCallableFactory(context, appMetricaConnector)
        );
    }

    private ReportsHandler(
        @NonNull final AppMetricaConnector appMetricaConnector,
        @NonNull final UnhandledExceptionEventFormer eventFormer,
        @NonNull final CommutationReporterEnvironment commutationReporterEnvironment,
        @NonNull final ServiceCallableFactory serviceCallableFactory
    ) {
        this(
            appMetricaConnector,
            eventFormer,
            commutationReporterEnvironment,
            serviceCallableFactory,
            new ReportsSender(appMetricaConnector, serviceCallableFactory)
        );
    }

    @VisibleForTesting
    ReportsHandler(
        @NonNull final AppMetricaConnector appMetricaConnector,
        @NonNull final UnhandledExceptionEventFormer eventFormer,
        @NonNull final CommutationReporterEnvironment commutationReporterEnvironment,
        @NonNull final ServiceCallableFactory serviceCallableFactory,
        @NonNull final ReportsSender reportsSender
    ) {
        mConnector = appMetricaConnector;
        mCommutationReportEnvironment = commutationReporterEnvironment;

        mEventFormer = eventFormer;
        this.serviceCallableFactory = serviceCallableFactory;
        mReportsSender = reportsSender;
    }

    void setShouldDisconnectFromServiceChecker(@Nullable final ShouldDisconnectFromServiceChecker checker) {
        serviceCallableFactory.setShouldDisconnectFromServiceChecker(checker);
    }

    void setStartupParamsProvider(final StartupIdentifiersProvider startupParamsProvider) {
        mStartupParamsProvider = startupParamsProvider;

        // Don't forget about anonymous environment, because it primarily needs of identifiers
        mCommutationReportEnvironment.setConfigIdentifiers(startupParamsProvider);
    }

    /*
        Workaround. Uses only main reporter's config. Fill be fixed in https://nda.ya.ru/t/Oczi4wyf6Njj6k
     */
    void updatePreActivationConfig(@Nullable Boolean locationTracking,
                                   @Nullable Boolean statisticsSending) {
        YLogger.info(TAG, "updatePreActivationConfig. locationTracking = %b, statisticsSending = %b",
            locationTracking, statisticsSending);
        if (Utils.isFieldSet(locationTracking)) {
            mCommutationReportEnvironment.getReporterConfiguration().setLocationTracking(locationTracking);
        }
        if (Utils.isFieldSet(statisticsSending)) {
            mCommutationReportEnvironment.getReporterConfiguration().setStatisticsSending(statisticsSending);
        }
        reportEvent(CounterReport.formUpdatePreActivationConfig(), mCommutationReportEnvironment);
    }

    public void onStartupRequestStarted() {
        mConnector.forbidDisconnect();
    }

    public void onStartupRequestFinished() {
        mConnector.allowDisconnect();
    }

    private CounterReport prepareRegularReport(final CounterReport event,
                                               final ReporterEnvironment reporterEnvironment) {
        if (EventsManager.shouldUseErrorEnvironment(event.getType())) {
            event.setEventEnvironment(reporterEnvironment.getErrorEnvironment());
        }
        return event;
    }

    void reportEvent(final CounterReport event, final ReporterEnvironment reporterEnvironment) {
        reportEvent(prepareRegularReport(event, reporterEnvironment), reporterEnvironment, null);
    }

    void reportEvent(
        final CounterReport event,
        final ReporterEnvironment reporterEnvironment,
        final Map<String, Object> attributes
    ) {
        reportEvent(event, reporterEnvironment, MetricaServiceDataReporter.TYPE_CORE, attributes);
    }

    public void reportEvent(
        CounterReport report,
        final ReporterEnvironment environment,
        final int serviceDataReporterType,
        final Map<String, Object> attributes
    ) {
        if (report.getType() == InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId()) {
            YLogger.d("Report event %s, value size: %d apiKey %s",
                InternalEvents.valueOf(report.getType()).toString(),
                report.getValueBytes().length,
                environment.getReporterConfiguration().getApiKey()
            );
        }
        mConnector.removeScheduleDisconnect();
        if (Utils.isNullOrEmpty(attributes) == false) {
            report.setValue(JsonHelper.mapToJsonString(attributes));
            prepareRegularReport(report, environment);
        }
        ReportToSend reportToSend = ReportToSend.newBuilder(report, environment)
            .withServiceDataReporterType(serviceDataReporterType)
            .build();
        queueReport(reportToSend);
    }

    public void reportResumeUserSession(@NonNull ProcessConfiguration processConfiguration) {
        YLogger.d("%sReport resume user session", TAG);
        mReportsSender.queueResumeUserSession(processConfiguration);
    }

    public void reportPauseUserSession(@NonNull ProcessConfiguration processConfiguration) {
        YLogger.d("%sReport pause user session", TAG);
        mReportsSender.queuePauseUserSession(processConfiguration);
    }

    public void reportStartupEvent(@NonNull List<String> identifiers,
                                   @NonNull ResultReceiver receiver,
                                   @Nullable Map<String, String> freshClientClids) {
        Bundle payload = new Bundle();
        payload.putParcelable(IdentifiersData.BUNDLE_KEY, new IdentifiersData(identifiers, freshClientClids, receiver));
        CounterReport counterReport = EventsManager.reportEntry(InternalEvents.EVENT_TYPE_STARTUP,
            LoggerStorage.getAnonymousPublicLogger());
        counterReport.setPayload(payload);
        reportEvent(counterReport, mCommutationReportEnvironment);
    }

    public void reportRequestReferrerEvent(@NonNull ReferrerResultReceiver receiver) {
        Bundle payload = new Bundle();
        payload.putParcelable(ReferrerResultReceiver.BUNDLE_KEY, receiver);
        CounterReport counterReport = EventsManager.requestReferrerEntry(LoggerStorage.getAnonymousPublicLogger());
        counterReport.setPayload(payload);
        reportEvent(counterReport, mCommutationReportEnvironment);
    }

    public void reportActivationEvent(final ReporterEnvironment environment) {
        reportEvent(
            EventsManager.activationEventReportEntry(
                environment.getPreloadInfoWrapper(),
                environment.getInitialUserProfileID(),
                getPublicLoggerForEnvironment(environment)
            ),
            environment
        );
    }

    public void setCustomHosts(final List<String> customHosts) {
        mCommutationReportEnvironment.getProcessConfiguration().setCustomHosts(customHosts);
    }

    public void setClids(final Map<String, String> clids) {
        mCommutationReportEnvironment.getProcessConfiguration().setClientClids(clids);
    }

    public void setDistributionReferrer(String referrer) {
        mCommutationReportEnvironment.getProcessConfiguration().setDistributionReferrer(referrer);
    }

    public void setInstallReferrerSource(String source) {
        mCommutationReportEnvironment.getProcessConfiguration().setInstallReferrerSource(source);
    }

    void reportUnhandledException(
        @NonNull UnhandledException unhandledException,
        final ReporterEnvironment reporterEnvironment) {
        queueReport(prepareUnhandledExceptionReport(unhandledException, reporterEnvironment));
    }

    void reportCrash(@NonNull UnhandledException unhandledException,
                     @NonNull ReporterEnvironment reporterEnvironment) {
        ReportToSend report = prepareUnhandledExceptionReport(unhandledException, reporterEnvironment);
        report.getEnvironment().updateStartupParams(mStartupParamsProvider);
        mReportsSender.sendCrash(report);
    }

    private ReportToSend prepareUnhandledExceptionReport(
        @NonNull UnhandledException unhandledException,
        @NonNull ReporterEnvironment reporterEnvironment) {
        YLogger.i("Unhandled exception was captured");
        mConnector.removeScheduleDisconnect();
        return mEventFormer.formEvent(unhandledException, reporterEnvironment);
    }

    void onResumeForegroundSession() {
        // Don't unbind, App became alive
        YLogger.info(TAG, "onResumeForegroundSession");
        mConnector.removeScheduleDisconnect();
    }

    void onPauseForegroundSession() {
        // Unbind, App became lifeless
        YLogger.info(TAG, "onPauseForegroundSession");
        mConnector.scheduleDisconnect();
    }

    public void sendAppEnvironmentValue(String key, String value, ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formAppEnvironmentChangedReport(key, value),
                reporterEnvironment
            ).build()
        );
    }

    public void sendClearAppEnvironment(final ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formAppEnvironmentClearedReport(),
                reporterEnvironment
            ).build()
        );
    }

    void sendUserProfile(@NonNull final Userprofile.Profile userProfile,
                         @NonNull ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formUserProfileEvent(userProfile),
                reporterEnvironment
            ).build()
        );
    }

    void setUserProfileID(@Nullable final String userProfileID,
                          @NonNull ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formSetUserProfileIDEvent(
                    userProfileID,
                    getPublicLoggerForEnvironment(reporterEnvironment)
                ),
                reporterEnvironment
            ).build()
        );
    }

    void sendRevenue(@NonNull final RevenueWrapper revenue,
                     @NonNull ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formRevenueEvent(getPublicLoggerForEnvironment(reporterEnvironment), revenue),
                reporterEnvironment
            ).build()
        );
    }

    void sendAdRevenue(
        @NonNull final AdRevenueWrapper adRevenue,
        @NonNull ReporterEnvironment reporterEnvironment) {
        queueReport(
            ReportToSend.newBuilder(
                ClientCounterReport.formAdRevenueEvent(getPublicLoggerForEnvironment(reporterEnvironment), adRevenue),
                reporterEnvironment
            ).build()
        );
    }

    void sendECommerce(@NonNull ProtoSerializable event,
                       @NonNull ReporterEnvironment environment) {
        for (final Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider> result : event.toProto()) {
            queueReport(
                ReportToSend.newBuilder(
                    ClientCounterReport.formECommerceEvent(getPublicLoggerForEnvironment(environment), result),
                    environment
                ).build()
            );
        }
    }

    private void queueReport(ReportToSend report) {
        // Take the last state for startup params
        report.getEnvironment().updateStartupParams(mStartupParamsProvider);

        mReportsSender.queueReport(report);
    }

    @NonNull
    public ReportsSender getServiceDataReporter() {
        return mReportsSender;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    ReporterEnvironment getCommutationReporterEnvironment() {
        return mCommutationReportEnvironment;
    }

    @NonNull
    private PublicLogger getPublicLoggerForEnvironment(@NonNull ReporterEnvironment environment) {
        return LoggerStorage.getOrCreatePublicLogger(environment.getReporterConfiguration().getApiKey());
    }
}
