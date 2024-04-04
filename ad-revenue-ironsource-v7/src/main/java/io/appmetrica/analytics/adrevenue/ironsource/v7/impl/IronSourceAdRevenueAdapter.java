package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import androidx.annotation.NonNull;
import com.ironsource.mediationsdk.IronSource;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class IronSourceAdRevenueAdapter {

    private static final String TAG = "[IronSourceAdRevenueAdapter]";

    public static void registerListener(@NonNull ClientContext clientContext) {
        YLogger.info(TAG, "registerListener");
        IronSource.addImpressionDataListener(new IronSourceAdRevenueDataListener(clientContext));
    }
}
