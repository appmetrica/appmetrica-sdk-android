package io.appmetrica.analytics.screenshot.impl

import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig

class ScreenshotObserver(
    private val clientContext: ClientContext,
    private val screenshotCapturedCallback: () -> Unit,
) : ContentObserver(clientContext.clientExecutorProvider.defaultExecutor.handler) {

    private val tag = "[ScreenshotObserver]"

    @Volatile
    private var clientSideContentObserverCaptorConfig: ClientSideContentObserverCaptorConfig? = null

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        DebugLogger.info(tag, "onChange: $uri")
        super.onChange(selfChange, uri)
        if (uri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER)) {
            detectScreenshot()
        }
    }

    private fun detectScreenshot() {
        DebugLogger.info(tag, "detectScreenshot")
        val localConfig = clientSideContentObserverCaptorConfig
        if (localConfig == null) {
            DebugLogger.info(tag, "clientContentObserverCaptorConfig is null")
            return
        }

        try {
            val columns = arrayOf(MediaStore.Images.Media.DATE_ADDED) + localConfig.mediaStoreColumnNames
            clientContext.context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                MediaStore.Images.Media.DATE_ADDED + " >= ?",
                arrayOf((SystemTimeProvider().currentTimeSeconds() - localConfig.detectWindowSeconds).toString()),
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    columns.forEach { column ->
                        DebugLogger.info(
                            tag,
                            "DATE_ADDED: ${cursor.getString(cursor.getColumnIndexOrThrow(column))}"
                        )
                    }
                    localConfig.mediaStoreColumnNames.forEach { column ->
                        val columnValue = cursor.getString(cursor.getColumnIndexOrThrow(column)).lowercase()
                        if (columnValue.contains(SCREENSHOTS_DIRECTORY)) {
                            DebugLogger.info(tag, "Screenshot detected")
                            screenshotCapturedCallback.invoke()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DebugLogger.error(tag, e, "Error while detecting screenshot: ${e.message}")
        }
    }

    fun updateConfig(config: ClientSideContentObserverCaptorConfig?) {
        clientSideContentObserverCaptorConfig = config
    }

    companion object {
        private val EXTERNAL_CONTENT_URI_MATCHER = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
        private const val SCREENSHOTS_DIRECTORY = "screenshot"
    }
}
