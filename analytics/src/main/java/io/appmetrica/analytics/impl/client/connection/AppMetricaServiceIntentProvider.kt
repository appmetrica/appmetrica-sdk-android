package io.appmetrica.analytics.impl.client.connection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.AppMetricaConnectionConstants
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppMetricaServiceIntentProvider {

    private val tag = "[AppMetricaServiceIntentProvider]"

    private val safePackageManager = SafePackageManager()

    fun getIntent(context: Context): Intent {
        val serviceDescription =
            ClientServiceLocator.getInstance().serviceDescriptionProvider.serviceDescription(context)

        DebugLogger.info(tag, "service description: $serviceDescription")

        return Intent(context, serviceDescription.serviceClass)
            .setAction(AppMetricaConnectionConstants.ACTION_CLIENT_CONNECTION)
            .setData(serviceDescription.toUri())
            .putExtras(context.getMetadata())
            .putExtra(AppMetricaConnectionConstants.EXTRA_SCREEN_SIZE, context.getScreenSize())
    }

    private fun ServiceDescription.toUri(): Uri =
        Uri.Builder()
            .scheme(serviceScheme)
            .authority(packageName)
            .path(AppMetricaConnectionConstants.PATH_CLIENT)
            .appendQueryParameter(AppMetricaConnectionConstants.PARAMETER_PID, Process.myPid().toString())
            .appendQueryParameter(AppMetricaConnectionConstants.PARAMETER_PSID, ProcessConfiguration.PROCESS_SESSION_ID)
            .build()

    private fun Context.getMetadata(): Bundle =
        safePackageManager.getApplicationInfo(this, packageName, PackageManager.GET_META_DATA)?.metaData ?: Bundle()

    private fun Context.getScreenSize(): String? {
        val screenInfo = ClientServiceLocator.getInstance().getScreenInfoRetriever().retrieveScreenInfo(this)
        return screenInfo?.let { JsonHelper.screenInfoToJsonString(it) }
    }
}
