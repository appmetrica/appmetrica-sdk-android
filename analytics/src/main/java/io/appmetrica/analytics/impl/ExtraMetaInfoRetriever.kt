package io.appmetrica.analytics.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val BUILD_ID_RESOURCE_NAME = "io.appmetrica.analytics.build_id"
private const val IS_OFFLINE_RESOURCE_NAME = "io.appmetrica.analytics.is_offline"
private const val APPMETRICA_PLUGIN_ID = "io.appmetrica.analytics.plugin_id"

class ExtraMetaInfoRetriever @VisibleForTesting internal constructor(
    private val context: Context,
    private val buildIdRetriever: StringResourceRetriever,
    private val isOfflineRetriever: BooleanResourceRetriever,
    private val packageManager: SafePackageManager
) {

    private val tag = "[ExtraMetaInfoRetriever]"

    constructor(context: Context) : this(
        context,
        StringResourceRetriever(context, BUILD_ID_RESOURCE_NAME),
        BooleanResourceRetriever(context, IS_OFFLINE_RESOURCE_NAME),
        SafePackageManager()
    )

    val buildId: String?
        get() = buildIdRetriever.resource.also {
            DebugLogger.info(tag, "Retrieved build_id: $it")
        }

    val isOffline: Boolean?
        get() = isOfflineRetriever.resource.also {
            DebugLogger.info(tag, "Retrieved is_offline: $it")
        }

    val pluginId: String?
        get() = packageManager.getApplicationMetaData(context)?.getString(APPMETRICA_PLUGIN_ID).also {
            DebugLogger.info(tag, "Retrieved appmetrica plugin id: $it")
        }
}
