package io.appmetrica.analytics.impl.request

internal interface HostsProvider {

    fun getHosts(): Collection<String>
}
