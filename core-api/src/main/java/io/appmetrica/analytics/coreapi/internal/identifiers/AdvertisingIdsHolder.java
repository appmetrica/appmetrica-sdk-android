package io.appmetrica.analytics.coreapi.internal.identifiers;

import androidx.annotation.NonNull;

public class AdvertisingIdsHolder {

    @NonNull
    private final AdTrackingInfoResult mGoogle;
    @NonNull
    private final AdTrackingInfoResult mHuawei;
    @NonNull
    private final AdTrackingInfoResult yandex;

    public AdvertisingIdsHolder() {
        this(
                new AdTrackingInfoResult(),
                new AdTrackingInfoResult(),
                new AdTrackingInfoResult()
        );
    }

    public AdvertisingIdsHolder(@NonNull AdTrackingInfoResult google,
                                @NonNull AdTrackingInfoResult huawei,
                                @NonNull AdTrackingInfoResult yandex) {
        mGoogle = google;
        mHuawei = huawei;
        this.yandex = yandex;
    }

    @NonNull
    public AdTrackingInfoResult getGoogle() {
        return mGoogle;
    }

    @NonNull
    public AdTrackingInfoResult getHuawei() {
        return mHuawei;
    }

    @NonNull
    public AdTrackingInfoResult getYandex() {
        return yandex;
    }

    @Override
    public String toString() {
        return "AdvertisingIdsHolder{" +
                "mGoogle=" + mGoogle +
                ", mHuawei=" + mHuawei +
                ", yandex=" + yandex +
                '}';
    }
}
