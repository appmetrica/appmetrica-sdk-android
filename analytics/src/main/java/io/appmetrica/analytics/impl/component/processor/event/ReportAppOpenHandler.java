package io.appmetrica.analytics.impl.component.processor.event;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class ReportAppOpenHandler extends ReportComponentHandler {

    private static final String TAG = "[ReportAppOpenHandler]";

    public ReportAppOpenHandler(final ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull final CounterReport reportData) {
        String eventValue = reportData.getValue();
        if (TextUtils.isEmpty(eventValue) == false) {
            try {
                JSONObject jsonObject = new JSONObject(eventValue);
                String type = jsonObject.optString(EventsManager.EVENT_OPEN_TYPE_KEY);
                if (EventsManager.EVENT_OPEN_TYPE_OPEN.equals(type)) {
                    getComponent().getVitalComponentDataProvider().incrementOpenId();
                    String deeplink = jsonObject.optString(EventsManager.EVENT_OPEN_LINK_KEY);
                    if (isReattribution(deeplink)) {
                        reportData.setAttributionIdChanged(true);
                        handleReattributionParameter();
                    }
                }
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
            }
        }
        return false;
    }

    private void handleReattributionParameter() {
        DebugLogger.INSTANCE.info(TAG, "Handle re-attribution");
        getComponent().getVitalComponentDataProvider().incrementAttributionId();
        getComponent().resetConfigHolder();
        getComponent().getEventFirstOccurrenceService().reset();
    }

    private boolean isReattribution(@Nullable String deeplink) {
        if (TextUtils.isEmpty(deeplink) == false) {
            try {
                Uri deeplinkUri = Uri.parse(deeplink);
                String referrer = deeplinkUri.getQueryParameter("referrer");
                if (TextUtils.isEmpty(referrer) == false) {
                    AttributionConfig attributionConfig = getComponent().getStartupState().getAttributionConfig();
                    DebugLogger.INSTANCE.info(
                        TAG,
                        "Checking if %s is reattribution with config %s",
                        deeplink,
                        attributionConfig
                    );
                    String[] queryPairs = referrer.split("&");
                    for (String queryPair : queryPairs) {
                        int idx = queryPair.indexOf("=");
                        if (idx >= 0) {
                            String key = Uri.decode(queryPair.substring(0, idx));
                            String value = Uri.decode(queryPair.substring(idx + 1));
                            if (checkParameter(key, value, attributionConfig)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e);
            }
        }
        return false;
    }

    private boolean checkParameter(@NonNull String key,
                                   @Nullable String value,
                                   @Nullable AttributionConfig attributionConfig) {
        if ("reattribution".equals(key) && "1".equals(value)) {
            return true;
        }
        if (attributionConfig != null) {
            for (Pair<String, AttributionConfig.Filter> condition : attributionConfig.deeplinkConditions) {
                if (Utils.areEqual(condition.first, key)) {
                    if (condition.second == null || condition.second.value.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
