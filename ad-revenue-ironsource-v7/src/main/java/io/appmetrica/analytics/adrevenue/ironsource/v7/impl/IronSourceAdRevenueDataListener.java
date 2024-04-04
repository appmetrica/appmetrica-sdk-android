package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class IronSourceAdRevenueDataListener implements ImpressionDataListener {

    private static final String TAG = "[IronSourceAdRevenueDataListener]";

    @NonNull
    private final ClientContext clientContext;
    @NonNull
    private final AdRevenueConverter adRevenueConverter = new AdRevenueConverter();

    public IronSourceAdRevenueDataListener(@NonNull ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public void onImpressionSuccess(@Nullable ImpressionData impressionData) {
        if (impressionData != null) {
            YLogger.info(TAG, "impressionData is " + impressionData.getAllData().toString());
            clientContext.getAutoAdRevenueReporter().reportAutoAdRevenue(adRevenueConverter.convert(impressionData));
        } else {
            YLogger.info(TAG, "impressionData is null");
        }
    }
}
