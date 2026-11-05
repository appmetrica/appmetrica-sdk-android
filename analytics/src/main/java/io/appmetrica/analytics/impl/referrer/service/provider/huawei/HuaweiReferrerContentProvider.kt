package io.appmetrica.analytics.impl.referrer.service.provider.huawei

import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class HuaweiReferrerContentProvider {
    private val tag = "[HuaweiReferrerContentProvider]"
    private val providerUri = "content://com.huawei.appmarket.commondata/item/5"

    // Official AppGallery ContentProvider indices.
    // https://developer.huawei.com/consumer/en/doc/AppGallery-connect-Guides/agdlink-analysis-refer-0000001117762116
    // INDEX_REFERRER_EX is supported only by AppGallery 10.5.0.300 and later.
    private val indexEnterAgTime = 1 // Time when a user is redirected to AppGallery from third-party media
    private val indexStartDownloadTime = 3 // Time when a user taps to download an app on AppGallery
    private val indexReferrerEx = 5 // Referrer parameter set in the download link
    private val requiredColumnCount = maxOf(indexEnterAgTime, indexStartDownloadTime, indexReferrerEx) + 1

    fun getReferrer(context: Context): ReferrerResult = try {
        DebugLogger.info(tag, "Try to get referrer from Huawei content provider")
        queryReferrer(context)?.use { cursor ->
            if (!cursor.moveToFirst()) error("Cursor is empty")

            if (cursor.columnCount < requiredColumnCount) {
                error("Cursor column count is insufficient: ${cursor.columnCount}")
            }

            val installReferrer = cursor.getString(indexReferrerEx)
            if (installReferrer.isNullOrBlank()) error("Referrer is empty")

            val referrer = ReferrerInfo(
                installReferrer,
                readTimestampSeconds(cursor, indexEnterAgTime, "enterAgTime"),
                readTimestampSeconds(cursor, indexStartDownloadTime, "startDownloadTime"),
                ReferrerInfo.Source.HMS,
            )
            DebugLogger.info(tag, "Successful get referrer from Huawei content provider: $referrer")
            ReferrerResult.Success(referrer)
        } ?: error("Not found content provider")
    } catch (e: Throwable) {
        val message = "Failed to get referrer from huawei content provider"
        DebugLogger.warning(tag, "$message: ${e.message}")
        ReferrerResult.Failure(message, e)
    }

    private fun queryReferrer(context: Context): Cursor? {
        return context.contentResolver
            .query(Uri.parse(providerUri), null, null, arrayOf(context.packageName), null)
    }

    private fun readTimestampSeconds(cursor: Cursor, index: Int, name: String): Long {
        return try {
            if (cursor.isNull(index)) {
                DebugLogger.warning(tag, "Timestamp $name at index $index is null, using 0")
                0L
            } else {
                cursor.getLong(index)
            }
        } catch (e: Throwable) {
            DebugLogger.warning(tag, "Failed to read timestamp $name at index $index: ${e.message}")
            0L
        }
    }
}
