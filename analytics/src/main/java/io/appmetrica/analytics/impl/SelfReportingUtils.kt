package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper

internal object SelfReportingUtils {

    @JvmStatic
    fun SelfReporterWrapper.reportUuidError(
        errorIdentifierPostfix: String,
        theOnlyTrueUuid: String?,
        backupUuid: String?
    ) {
        val errorType = if (theOnlyTrueUuid == null) "null_uuid" else "wrong_uuid"
        reportError(
            "${errorType}_on_$errorIdentifierPostfix",
            "The only true uuid: $theOnlyTrueUuid; backup uuid: $backupUuid"
        )
    }
}
