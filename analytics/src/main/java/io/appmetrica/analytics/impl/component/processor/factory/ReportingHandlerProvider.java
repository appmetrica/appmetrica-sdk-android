package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ApplySettingsFromActivationConfigHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentClearedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentUpdatedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppOpenHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportCrashMetaInformation;
import io.appmetrica.analytics.impl.component.processor.event.ReportFeaturesHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstOccurrenceStatusHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPermissionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPrevSessionNativeCrashHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPurgeBufferHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveToDatabaseHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSessionHandler;
import io.appmetrica.analytics.impl.component.processor.event.SaveInitialUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.SavePreloadInfoHandler;
import io.appmetrica.analytics.impl.component.processor.event.SaveSessionExtrasHandler;
import io.appmetrica.analytics.impl.component.processor.event.SubscribeForReferrerHandler;
import io.appmetrica.analytics.impl.component.processor.event.ExternalAttributionHandler;
import io.appmetrica.analytics.impl.component.processor.event.UpdateUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.modules.ModulesEventHandler;
import io.appmetrica.analytics.impl.component.processor.session.ReportSessionStopHandler;
import io.appmetrica.analytics.impl.permissions.PermissionsChecker;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;

public class ReportingHandlerProvider {

    private final ReportPurgeBufferHandler mReportPurgeBufferHandler;
    private final ReportSaveToDatabaseHandler mReportSaveToDatabaseHandler;
    private final ReportSessionHandler mReportSessionHandler;
    private final ReportSessionStopHandler mReportSessionStopHandler;
    private final ReportAppEnvironmentUpdatedHandler mReportAppEnvironmentUpdated;
    private final ReportAppEnvironmentClearedHandler mReportAppEnvironmentCleared;
    private final ReportFirstHandler mReportFirstHandler;
    private final ReportPrevSessionNativeCrashHandler mReportPrevSessionNativeCrashHandler;
    private final ReportPermissionHandler mReportPermissionsHandler;
    private final ReportFeaturesHandler mReportFeaturesHandler;
    private final UpdateUserProfileIDHandler updateUserProfileIDHandler;
    private final ReportAppOpenHandler mReportAppOpenHandler;
    private final ReportFirstOccurrenceStatusHandler mReportFirstOccurrenceStatusHandler;
    private final ReportCrashMetaInformation reportCrashMetaInformation;
    private final SavePreloadInfoHandler mSavePreloadInfoHandler;
    private final ApplySettingsFromActivationConfigHandler mApplySettingsFromActivationConfigHandler;
    private final SubscribeForReferrerHandler mSubscribeForReferrerHandler;
    private final SaveInitialUserProfileIDHandler saveInitialUserProfileIDHandler;
    private final ModulesEventHandler modulesEventHandler;
    private final SaveSessionExtrasHandler saveSessionExtrasHandler;
    private final ExternalAttributionHandler externalAttributionHandler;

    public ReportingHandlerProvider(ComponentUnit component) {
        mReportPurgeBufferHandler = new ReportPurgeBufferHandler(component);
        mReportSaveToDatabaseHandler = new ReportSaveToDatabaseHandler(component);
        mReportSessionHandler = new ReportSessionHandler(component);
        mReportSessionStopHandler = new ReportSessionStopHandler(component);
        mReportAppEnvironmentUpdated = new ReportAppEnvironmentUpdatedHandler(component);
        mReportAppEnvironmentCleared = new ReportAppEnvironmentClearedHandler(component);
        mReportFirstHandler = new ReportFirstHandler(component);
        mReportPrevSessionNativeCrashHandler = new ReportPrevSessionNativeCrashHandler(component);
        mReportPermissionsHandler = new ReportPermissionHandler(
                component,
                new PermissionsChecker()
        );
        mReportFeaturesHandler = new ReportFeaturesHandler(component);
        updateUserProfileIDHandler = new UpdateUserProfileIDHandler(component);
        mReportAppOpenHandler = new ReportAppOpenHandler(component);
        mReportFirstOccurrenceStatusHandler = new ReportFirstOccurrenceStatusHandler(component);
        reportCrashMetaInformation = new ReportCrashMetaInformation(
                component,
                AppMetricaSelfReportFacade.getReporter()
        );
        mSavePreloadInfoHandler = new SavePreloadInfoHandler(component);
        mApplySettingsFromActivationConfigHandler = new ApplySettingsFromActivationConfigHandler(component);
        mSubscribeForReferrerHandler = new SubscribeForReferrerHandler(component);
        saveInitialUserProfileIDHandler = new SaveInitialUserProfileIDHandler(component);
        modulesEventHandler = new ModulesEventHandler(component);
        saveSessionExtrasHandler = new SaveSessionExtrasHandler(component);
        externalAttributionHandler = new ExternalAttributionHandler(component, new SystemTimeProvider());
    }

    public ReportPurgeBufferHandler getReportPurgeBufferHandler() {
        return mReportPurgeBufferHandler;
    }

    public ReportSaveToDatabaseHandler getReportSaveToDatabaseHandler() {
        return mReportSaveToDatabaseHandler;
    }

    public ReportSessionHandler getReportSessionHandler() {
        return mReportSessionHandler;
    }

    public ReportSessionStopHandler getReportSessionStopHandler() {
        return mReportSessionStopHandler;
    }

    public ReportAppEnvironmentUpdatedHandler getReportAppEnvironmentUpdated() {
        return mReportAppEnvironmentUpdated;
    }

    public ReportAppEnvironmentClearedHandler getReportAppEnvironmentCleared() {
        return mReportAppEnvironmentCleared;
    }

    public ReportFirstHandler getReportFirstHandler() {
        return mReportFirstHandler;
    }

    public ReportPrevSessionNativeCrashHandler getReportPrevSessionNativeCrashHandler() {
        return mReportPrevSessionNativeCrashHandler;
    }

    public ReportPermissionHandler getReportPermissionsHandler() {
        return mReportPermissionsHandler;
    }

    public ReportFeaturesHandler getReportFeaturesHandler() {
        return mReportFeaturesHandler;
    }

    public UpdateUserProfileIDHandler getUpdateUserProfileIDHandler() {
        return updateUserProfileIDHandler;
    }

    public SaveInitialUserProfileIDHandler getSaveInitialUserProfileIDHandler() {
        return saveInitialUserProfileIDHandler;
    }

    public ReportAppOpenHandler getReportAppOpenHandler() {
        return mReportAppOpenHandler;
    }

    public ReportFirstOccurrenceStatusHandler getReportFirstOccurrenceStatusHandler() {
        return mReportFirstOccurrenceStatusHandler;
    }

    public ReportCrashMetaInformation getReportCrashMetaInformation() {
        return reportCrashMetaInformation;
    }

    @NonNull
    public SavePreloadInfoHandler getSavePreloadInfoHandler() {
        return mSavePreloadInfoHandler;
    }

    @NonNull
    public ApplySettingsFromActivationConfigHandler getApplySettingsFromActivationConfigHandler() {
        return mApplySettingsFromActivationConfigHandler;
    }

    @NonNull
    public SubscribeForReferrerHandler getSubscribeForReferrerHandler() {
        return mSubscribeForReferrerHandler;
    }

    @NonNull
    public ModulesEventHandler getModulesEventHandler() {
        return this.modulesEventHandler;
    }

    @NonNull
    public SaveSessionExtrasHandler getSaveSessionExtrasHandler() {
        return saveSessionExtrasHandler;
    }

    @NonNull
    public ExternalAttributionHandler getExternalAttributionHandler() {
        return externalAttributionHandler;
    }
}
