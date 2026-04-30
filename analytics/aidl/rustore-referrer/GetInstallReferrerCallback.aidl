package ru.vk.store.sdk.install.referrer;

/**
 * @see io.appmetrica.analytics.impl.referrer.service.provider.rustore.RuStoreReferrerService
 * @see io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
 * @see https://nda.ya.ru/t/KKJAcQS67aqrVp
 */
interface GetInstallReferrerCallback {
    void onSuccess(String payload);

    void onError(int code, String errorMessage);
}
