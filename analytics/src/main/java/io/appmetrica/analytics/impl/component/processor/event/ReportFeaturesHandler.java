package io.appmetrica.analytics.impl.component.processor.event;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.features.FeatureAdapter;
import io.appmetrica.analytics.impl.features.FeatureDescription;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportFeaturesHandler extends ReportComponentHandler {

    private static final String TAG = "[ReportFeaturesHandler]";

    @NonNull
    private final SafePackageManager mSafePackageManager;

    public ReportFeaturesHandler(ComponentUnit component) {
        this(component, new SafePackageManager());
    }

    @VisibleForTesting
    public ReportFeaturesHandler(ComponentUnit component, @NonNull SafePackageManager safePackageManager) {
        super(component);
        mSafePackageManager = safePackageManager;
    }

    public boolean process(@NonNull CounterReport reportData) {
        ComponentUnit component = getComponent();

        VitalComponentDataProvider vitalComponentDataProvider = component.getVitalComponentDataProvider();
        if (vitalComponentDataProvider.isFirstEventDone() && component.needToCollectFeatures()) {
            PreferencesComponentDbStorage componentPreferences = component.getComponentPreferences();
            HashSet<FeatureDescription> fromDB = parseFeaturesFromStorage();
            try {
                ArrayList<FeatureDescription> fromSystem = getFeaturesFromSystem();
                if (CollectionUtils.areCollectionsEqual(fromDB, fromSystem)) {
                    component.markFeaturesChecked();
                } else {
                    JSONArray newFeatures = new JSONArray();
                    for (FeatureDescription description : fromSystem) {
                        newFeatures.put(description.toJSON());
                    }
                    String featuresToSend = new JSONObject().put("features", newFeatures).toString();
                    CounterReport permissionsReport = CounterReport.formFeaturesReportData(reportData, featuresToSend);
                    component.getEventSaver().saveFeaturesReport(permissionsReport);
                    componentPreferences.putApplicationFeatures(newFeatures.toString());
                }
            } catch (Throwable e) {
                DebugLogger.error(TAG, e, "can't write features");
            }
        }
        return false;
    }

    @VisibleForTesting()
    @Nullable
    HashSet<FeatureDescription> parseFeaturesFromStorage() {
        PreferencesComponentDbStorage componentPreferences = getComponent().getComponentPreferences();
        String featuresJSON = componentPreferences.getApplicationFeatures();
        if (TextUtils.isEmpty(featuresJSON)) {
            return null;
        } else {
            try {
                HashSet<FeatureDescription> fromDB = new HashSet<FeatureDescription>();
                JSONArray array = new JSONArray(featuresJSON);
                for (int i = 0; i < array.length(); i++) {
                    fromDB.add(new FeatureDescription(array.getJSONObject(i)));
                }
                return fromDB;
            } catch (Throwable e) {
                DebugLogger.error(TAG, e, "can't parse features");
                return null;
            }
        }
    }

    @VisibleForTesting
    @Nullable
    ArrayList<FeatureDescription> getFeaturesFromSystem() {
        try {
            ComponentUnit component = getComponent();
            PackageInfo info = mSafePackageManager.getPackageInfo(
                    component.getContext(),
                    component.getContext().getPackageName(),
                    PackageManager.GET_CONFIGURATIONS
            );
            ArrayList<FeatureDescription> fromSystem = new ArrayList<FeatureDescription>();

            FeatureAdapter adapter = FeatureAdapter.Factory.create();
            if (info != null && info.reqFeatures != null) {
                for (FeatureInfo feature : info.reqFeatures) {
                    fromSystem.add(adapter.adapt(feature));
                }
            }
            return fromSystem;
        } catch (Throwable e) {
            DebugLogger.error(TAG, e, "can't get features from system");
            return null;
        }
    }
}
