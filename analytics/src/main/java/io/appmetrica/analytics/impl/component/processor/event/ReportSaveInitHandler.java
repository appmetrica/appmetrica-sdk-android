package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.logger.internal.DebugLogger;
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
    public boolean process(@NonNull final CounterReport reportData) {
        ComponentUnit component = getComponent();
        if (vitalComponentDataProvider.isInitEventDone() == false) {
            CounterReport reportToSave;

            if (component.getFreshReportRequestConfig().isFirstActivationAsUpdate()) {
                reportToSave = CounterReport.formUpdateReportData(reportData);
            } else {
                reportToSave = CounterReport.formInitReportData(reportData);
            }
            final JSONObject eventValue = new JSONObject();
            final String packageInstaller = WrapUtils.getOrDefault(
                    mPackageManager.getInstallerPackageName(
                            component.getContext(),
                            component.getComponentId().getPackage()
                    ),
                    StringUtils.EMPTY
            );
            DebugLogger.info(TAG, "PackageInstaller = %s", packageInstaller);
            try {
                eventValue.put(JsonKeys.APP_INSTALLER, packageInstaller);
                eventValue.put(
                        JsonKeys.PRELOAD_INFO,
                        mPreloadInfoStorage.retrieveData().toEventJson()
                );
            } catch (Throwable ex) {
                DebugLogger.error(TAG, ex);
            }
            DebugLogger.info(TAG, "save init event: %s", eventValue);
            reportToSave.setValue(eventValue.toString());
            component.getEventSaver().identifyAndSaveReport(reportToSave);
            vitalComponentDataProvider.setInitEventDone(true);
            vitalComponentDataProvider.setExternalAttributionWindowStart(timeProvider.currentTimeMillis());
        } else {
            DebugLogger.info(TAG, "Event init has already been sent");
        }
        return false;
    }
}
