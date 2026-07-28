package io.appmetrica.analytics.impl.request

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StringArrayResourceRetriever

internal class ResourceStartupHostsProvider @VisibleForTesting constructor(
    private val hostsFromResourcesRetriever: StringArrayResourceRetriever
) : HostsProvider {

    constructor() : this(
        StringArrayResourceRetriever(
            GlobalServiceLocator.getInstance().context,
            Constants.CUSTOM_DEFAULT_HOSTS_RESOURCE_NAME
        )
    )

    override fun getHosts(): Collection<String> {
        return hostsFromResourcesRetriever.resource?.removeBlankElements() ?: emptyList()
    }

    private fun Array<String?>.removeBlankElements(): List<String> = mapNotNull { it?.takeIf { it.isNotBlank() } }
}
