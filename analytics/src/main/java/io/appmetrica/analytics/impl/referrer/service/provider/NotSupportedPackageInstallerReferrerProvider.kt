package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult

internal class NotSupportedPackageInstallerReferrerProvider(
    private val packageInstaller: String?,
) : ReferrerProvider {
    override val referrerName: String = packageInstaller ?: "<unknown-package-installer>"

    override fun requestReferrer(listener: ReferrerListener) {
        listener.onResult(ReferrerResult.Failure("Package installer $packageInstaller is not supported"))
    }
}
