package ru.vk.store.sdk.install.referrer;

import ru.vk.store.sdk.install.referrer.GetInstallReferrerCallback;

/**
 * @see io.appmetrica.analytics.impl.referrer.service.provider.rustore.RuStoreReferrerService
 * @see io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
 * @see https://nda.ya.ru/t/2aPko5or7aqre2
 */
interface InstallReferrerProvider {
    void getInstallReferrer(String packageName, GetInstallReferrerCallback installReferrerCallback);
}
