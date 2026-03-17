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
    private val referrerIndex = 0
    private val clickDownloadIndex = 1
    private val installTimeIndex = 2

    fun getReferrer(context: Context): ReferrerResult = try {
        DebugLogger.info(tag, "Try to get referrer from Huawei content provider")
        queryReferrer(context)?.use { cursor ->
            if (!cursor.moveToFirst()) error("Cursor is empty")

            val installReferrer = cursor.getString(referrerIndex)
            if (installReferrer.isNullOrBlank()) error("Referrer is empty")

            val referrer = ReferrerInfo(
                installReferrer,
                cursor.getLong(clickDownloadIndex),
                cursor.getLong(installTimeIndex),
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
}
