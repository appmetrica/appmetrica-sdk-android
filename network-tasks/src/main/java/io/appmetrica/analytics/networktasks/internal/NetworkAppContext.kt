package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter
import io.appmetrica.analytics.coreapi.internal.system.LocaleProvider

interface NetworkAppContext {

    val sdkInfo: SdkInfo

    val appInfo: AppInfo

    val screenInfoProvider: ScreenInfoProvider

    val advertisingIdGetter: SimpleAdvertisingIdGetter

    val localeProvider: LocaleProvider

    val appSetIdProvider: AppSetIdProvider
}
