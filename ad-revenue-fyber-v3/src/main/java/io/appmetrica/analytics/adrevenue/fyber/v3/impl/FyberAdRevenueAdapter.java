package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import com.fyber.fairbid.ads.Interstitial;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class FyberAdRevenueAdapter {

    private static final String TAG = "[FyberAdRevenueAdapter]";

    public static void registerListener(@NonNull ClientContext clientContext) {
        YLogger.info(TAG, "registerListener");
        Interstitial.setInterstitialListener(new FyberAdRevenueDataListener(clientContext));
    }
}