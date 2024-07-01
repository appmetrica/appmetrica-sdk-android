package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.interstitial.InterstitialListener;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class FyberAdRevenueDataListener implements InterstitialListener {

    private static final String TAG = "[FyberAdRevenueDataListener]";

    @NonNull
    private final ClientContext clientContext;
    @NonNull
    private final AdRevenueConverter adRevenueConverter = new AdRevenueConverter();

    public FyberAdRevenueDataListener(@NonNull ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public void onUnavailable(@NonNull String s) {
        // do nothing
    }

    @Override
    public void onShowFailure(@NonNull String s, @NonNull ImpressionData impressionData) {
        // do nothing
    }

    @Override
    public void onShow(@NonNull String s, @NonNull ImpressionData impressionData) {
        if (impressionData != null) {
            DebugLogger.INSTANCE.info(TAG, "impressionData is " + impressionData.getJsonString());
            clientContext.getModuleAdRevenueContext().getAdRevenueReporter()
                .reportAutoAdRevenue(adRevenueConverter.convert(impressionData));
        } else {
            DebugLogger.INSTANCE.info(TAG, "impressionData is null");
        }
    }

    @Override
    public void onRequestStart(@NonNull String s, @NonNull String s1) {
        // do nothing
    }

    @Override
    public void onHide(@NonNull String s) {
        // do nothing
    }

    @Override
    public void onClick(@NonNull String s) {
        // do nothing
    }

    @Override
    public void onAvailable(@NonNull String s) {
        // do nothing
    }
}
