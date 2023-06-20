package io.appmetrica.analytics.impl

import android.content.ContentValues
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[ContentProviderHelper]"

internal class ContentProviderHelper<T> @VisibleForTesting constructor(
    private val dataParser: ContentProviderDataParser<T>,
    private val dataSaver: ContentProviderDataSaver<T>,
    private val firstServiceEntryPointManager: FirstServiceEntryPointManager,
    private val description: String
) {

    constructor(
        dataParser: ContentProviderDataParser<T>,
        dataSaver: ContentProviderDataSaver<T>,
        description: String
    ) : this(dataParser, dataSaver, FirstServiceEntryPointManager.INSTANCE, description)

    fun handle(context: Context, values: ContentValues) {
        YLogger.info(TAG, "Try to handle %s", values)
        try {
            val parsedData = dataParser(values)
            YLogger.info(TAG, "Parsed data: %s", parsedData)
            if (parsedData != null) {
                val result = saveData(context, parsedData)
                YLogger.info(TAG, "Saved data? %b", result)
                if (result) {
                    SdkUtils.logAttribution("Successfully saved $description")
                } else {
                    SdkUtils.logAttributionW("Did not save $description because data is already present")
                }
            }
        } catch (ex: Throwable) {
            YLogger.error(TAG, ex)
            SdkUtils.logAttributionE(ex, "Unexpected error occurred")
        }
    }

    @MainThread
    private fun saveData(context: Context, data: T): Boolean {
        firstServiceEntryPointManager.onPossibleFirstEntry(context)
        return dataSaver(data)
    }
}
