package io.appmetrica.analytics.impl

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter
import io.appmetrica.analytics.coreapi.internal.system.LocaleProvider
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.networktasks.internal.AppInfo
import io.appmetrica.analytics.networktasks.internal.NetworkAppContext
import io.appmetrica.analytics.networktasks.internal.ScreenInfoProvider
import io.appmetrica.analytics.networktasks.internal.SdkInfo

class NetworkAppContextProvider {

    fun getNetworkAppContext(): NetworkAppContext {
        return object : NetworkAppContext {
            override val sdkInfo: SdkInfo = object : SdkInfo {

                override val sdkVersionName: String = BuildConfig.VERSION_NAME

                override val sdkBuildNumber: String = BuildConfig.BUILD_NUMBER

                override val sdkBuildType: String =
                    "${BuildConfig.SDK_BUILD_FLAVOR}_${BuildConfig.SDK_DEPENDENCY}_${BuildConfig.SDK_BUILD_TYPE}"
            }

            override val appInfo: AppInfo = object : AppInfo {
                override val appFramework: String = FrameworkDetector.framework()
            }

            override val screenInfoProvider: ScreenInfoProvider
                get() = GlobalServiceLocator.getInstance().screenInfoHolder

            override val localeProvider: LocaleProvider
                get() = LocaleHolder.getInstance(GlobalServiceLocator.getInstance().context)

            override val advertisingIdGetter: SimpleAdvertisingIdGetter
                get() = GlobalServiceLocator.getInstance().serviceInternalAdvertisingIdGetter

            override val appSetIdProvider: AppSetIdProvider
                get() = GlobalServiceLocator.getInstance().appSetIdGetter
        }
    }
}
