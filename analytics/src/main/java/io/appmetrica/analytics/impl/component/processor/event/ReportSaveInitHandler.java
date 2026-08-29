package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class ReportSaveInitHandler extends ReportComponentHandler {

    private static final String TAG = "[ReportSaveInitHandler]";

    public static class JsonKeys {
        private static final String APP_INSTALLER = "appInstaller";
        public static final String PRELOAD_INFO = "preloadInfo";
    }

    @NonNull
    private final PreloadInfoStorage mPreloadInfoStorage;
    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;
    @NonNull
    private final SafePackageManager mPackageManager;
    @NonNull
    private final TimeProvider timeProvider;

    public ReportSaveInitHandler(@NonNull ComponentUnit component) {
        this(
            component,
            component.getVitalComponentDataProvider(),
            GlobalServiceLocator.getInstance().getPreloadInfoStorage(),
            new SafePackageManager(),
            new SystemTimeProvider()
        );
    }

    @VisibleForTesting
    ReportSaveInitHandler(
        @NonNull final ComponentUnit component,
        @NonNull VitalComponentDataProvider vitalComponentDataProvider,
        @NonNull PreloadInfoStorage preloadInfoStorage,
        @NonNull SafePackageManager safePackageManager,
        @NonNull final TimeProvider timeProvider
    ) {
        super(component);
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        mPreloadInfoStorage = preloadInfoStorage;
        mPackageManager = safePackageManager;
        this.timeProvider = timeProvider;
    }

    @Override
    public boolean process(@NonNull final CoreServiceEvent serviceEvent) {
        ComponentUnit component = getComponent();
        if (vitalComponentDataProvider.isInitEventDone() == false) {
            CoreServiceEvent serviceEventToSave;

            if (component.getFreshReportRequestConfig().isFirstActivationAsUpdate()) {
                serviceEventToSave = CoreServiceEvent.formUpdateReportData(serviceEvent);
            } else {
                serviceEventToSave = CoreServiceEvent.formInitReportData(serviceEvent);
            }
            final JSONObject eventValue = new JSONObject();
            final String packageInstaller = WrapUtils.getOrDefault(
                    mPackageManager.getInstallerPackageName(
                            component.getContext(),
                            component.getComponentId().getPackage()
                    ),
                    StringUtils.EMPTY
            );
            DebugLogger.INSTANCE.info(TAG, "PackageInstaller = %s", packageInstaller);
            try {
                eventValue.put(JsonKeys.APP_INSTALLER, packageInstaller);
                eventValue.put(
                        JsonKeys.PRELOAD_INFO,
                        mPreloadInfoStorage.retrieveData().toEventJson()
                );
            } catch (Throwable ex) {
                DebugLogger.INSTANCE.error(TAG, ex);
            }
            DebugLogger.INSTANCE.info(TAG, "save init event: %s", eventValue);
            serviceEventToSave.setValue(eventValue.toString());
            component.getEventSaver().identifyAndSaveReport(serviceEventToSave);
            vitalComponentDataProvider.setInitEventDone(true);
            vitalComponentDataProvider.setExternalAttributionWindowStart(timeProvider.currentTimeMillis());
        } else {
            DebugLogger.INSTANCE.info(TAG, "Event init has already been sent");
        }
        return false;
    }
}
