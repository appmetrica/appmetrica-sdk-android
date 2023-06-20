package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreutils.internal.network.UserAgent

internal class UserAgentProvider {

    val userAgent = UserAgent.getFor(
        Constants.Config.LIBRARY_ID,
        BuildConfig.VERSION_NAME,
        BuildConfig.BUILD_NUMBER
    )
}
