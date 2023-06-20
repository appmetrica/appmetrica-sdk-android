package io.appmetrica.analytics.impl.request

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StringArrayResourceRetriever

internal class DefaultStartupHostsProvider @VisibleForTesting constructor(
    private val hostsFromResourcesRetriever: StringArrayResourceRetriever
) {

    constructor() : this(
        StringArrayResourceRetriever(
            GlobalServiceLocator.getInstance().context,
            Constants.CUSTOM_DEFAULT_HOSTS_RESOURCE_NAME
        )
    )

    fun getDefaultHosts(): Collection<String> {
        return hostsFromResourcesRetriever.resource?.removeBlankElements()?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.DEFAULT_HOSTS.removeBlankElements()
    }

    private fun Array<String?>.removeBlankElements(): List<String> = mapNotNull { it?.takeIf { it.isNotBlank() } }
}
