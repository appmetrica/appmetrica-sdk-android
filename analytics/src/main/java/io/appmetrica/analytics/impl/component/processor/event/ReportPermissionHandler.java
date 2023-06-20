package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.AppStandbyBucketConverter;
import io.appmetrica.analytics.impl.AvailableProvidersRetriever;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.BackgroundRestrictionsStateProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.permissions.PermissionsChecker;
import java.util.List;

public class ReportPermissionHandler extends ReportComponentHandler {

    private final PermissionsChecker mPermissionsChecker;
    @NonNull
    private final ProtobufStateStorage<AppPermissionsState> mPermissionsStorage;
    @NonNull
    private final BackgroundRestrictionsStateProvider mBackgroundRestrictionsStateProvider;
    @NonNull
    private final AppStandbyBucketConverter mAppStandbyBucketConverter;
    @NonNull
    private final AvailableProvidersRetriever mAvailableProvidersRetriever;

    public ReportPermissionHandler(ComponentUnit component,
                                   PermissionsChecker permissionsChecker) {
        this(
                component,
                permissionsChecker,
                StorageFactory.Provider
                        .get(AppPermissionsState.class)
                        .create(component.getContext()),
                new BackgroundRestrictionsStateProvider(component.getContext()),
                new AppStandbyBucketConverter(),
                new AvailableProvidersRetriever(component.getContext())
        );
    }

    @VisibleForTesting
    ReportPermissionHandler(ComponentUnit component,
                            PermissionsChecker permissionsChecker,
                            @NonNull ProtobufStateStorage<AppPermissionsState> permissionsStorage,
                            @NonNull BackgroundRestrictionsStateProvider backgroundRestrictionsStateProvider,
                            @NonNull AppStandbyBucketConverter appStandbyBucketConverter,
                            @NonNull AvailableProvidersRetriever availableProvidersRetriever) {
        super(component);
        mPermissionsChecker = permissionsChecker;
        mPermissionsStorage = permissionsStorage;
        mBackgroundRestrictionsStateProvider = backgroundRestrictionsStateProvider;
        mAppStandbyBucketConverter = appStandbyBucketConverter;
        mAvailableProvidersRetriever = availableProvidersRetriever;
    }

    public boolean process(@NonNull CounterReport reportData) {
        ComponentUnit component = getComponent();
        String componentIdName = component.getComponentId().toString();

        if (component.getVitalComponentDataProvider().isFirstEventDone() && component.needToCheckPermissions()) {
            YLogger.d("Sending PermissionsChecker Event for %s", componentIdName);

            AppPermissionsState oldAppPermissionsState = mPermissionsStorage.read();
            AppPermissionsState newAppPermissionsState = getNewAppPermissionsStateOrNull(oldAppPermissionsState);
            if (newAppPermissionsState == null) {
                if (component.shouldForceSendPermissions()) {
                    reportPermissions(
                            oldAppPermissionsState,
                            reportData,
                            component.getEventSaver()
                    );
                }
            } else {
                reportPermissions(
                        newAppPermissionsState,
                        reportData,
                        component.getEventSaver()
                );
                mPermissionsStorage.save(newAppPermissionsState);
            }
        }
        return false;
    }

    @Nullable
    private AppPermissionsState getNewAppPermissionsStateOrNull(@NonNull AppPermissionsState oldAppPermissionsState) {
        List<PermissionState> permissionsFromDb = oldAppPermissionsState.mPermissionStateList;
        BackgroundRestrictionsState oldBackgroundRestrictionsState =
                oldAppPermissionsState.mBackgroundRestrictionsState;
        BackgroundRestrictionsState newBackgroundRestrictionsState = mBackgroundRestrictionsStateProvider
                .getBackgroundRestrictionsState();
        List<String> oldProviders = oldAppPermissionsState.mAvailableProviders;
        List<String> newProviders = mAvailableProvidersRetriever.getAvailableProviders();
        List<PermissionState> newPermissions =
                mPermissionsChecker.check(getComponent().getContext(), permissionsFromDb);
        if (newPermissions == null &&
                Utils.areEqual(oldBackgroundRestrictionsState, newBackgroundRestrictionsState) &&
                CollectionUtils.areCollectionsEqual(oldProviders, newProviders)) {
            return null;
        } else {

            return new AppPermissionsState(
                    newPermissions == null ? permissionsFromDb : newPermissions,
                    newBackgroundRestrictionsState,
                    newProviders
            );
        }

    }

    private void reportPermissions(@NonNull AppPermissionsState appPermissionsState,
                                   @NonNull CounterReport reportData,
                                   @NonNull EventSaver eventSaver) {
        CounterReport permissionsReport = CounterReport.formPermissionsReportData(
                reportData,
                appPermissionsState.mPermissionStateList,
                appPermissionsState.mBackgroundRestrictionsState,
                mAppStandbyBucketConverter,
                appPermissionsState.mAvailableProviders
        );
        eventSaver.savePermissionsReport(permissionsReport);
    }
}
