package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.BuildConfig

internal class DefaultStartupHostsProvider : HostsProvider {

    override fun getHosts(): Collection<String> {
        return BuildConfig.DEFAULT_HOSTS.removeBlankElements()
    }

    private fun Array<String?>.removeBlankElements(): List<String> = mapNotNull { it?.takeIf { it.isNotBlank() } }
}
