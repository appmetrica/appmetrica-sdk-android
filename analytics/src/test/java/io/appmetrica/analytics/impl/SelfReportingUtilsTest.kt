package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.SelfReportingUtils.reportUuidError
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class SelfReportingUtilsTest : CommonTest() {

    private val selfReporter: SelfReporterWrapper = mock()
    private val errorIdentifierPostfix = "startup_init"

    @Test
    fun reportUuidErrorWhenTheOnlyTrueUuidIsNull() {
        val theOnlyTrueUuid: String? = null
        val backupUuid = "backup-uuid-123"

        selfReporter.reportUuidError(errorIdentifierPostfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "null_uuid_on_startup_init",
            "The only true uuid: null; backup uuid: backup-uuid-123"
        )
    }

    @Test
    fun reportUuidErrorWhenTheOnlyTrueUuidIsNullAndBackupIsNull() {
        val theOnlyTrueUuid: String? = null
        val backupUuid: String? = null

        selfReporter.reportUuidError(errorIdentifierPostfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "null_uuid_on_startup_init",
            "The only true uuid: null; backup uuid: null"
        )
    }

    @Test
    fun reportUuidErrorWhenTheOnlyTrueUuidIsNotNull() {
        val theOnlyTrueUuid = "true-uuid-456"
        val backupUuid = "backup-uuid-123"

        selfReporter.reportUuidError(errorIdentifierPostfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "wrong_uuid_on_startup_init",
            "The only true uuid: true-uuid-456; backup uuid: backup-uuid-123"
        )
    }

    @Test
    fun reportUuidErrorWhenTheOnlyTrueUuidIsNotNullAndBackupIsNull() {
        val theOnlyTrueUuid = "true-uuid-789"
        val backupUuid: String? = null

        selfReporter.reportUuidError(errorIdentifierPostfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "wrong_uuid_on_startup_init",
            "The only true uuid: true-uuid-789; backup uuid: null"
        )
    }

    @Test
    fun reportUuidErrorWithDifferentPostfix() {
        val postfix = "params_constructor"
        val theOnlyTrueUuid = "uuid-abc"
        val backupUuid = "uuid-def"

        selfReporter.reportUuidError(postfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "wrong_uuid_on_params_constructor",
            "The only true uuid: uuid-abc; backup uuid: uuid-def"
        )
    }

    @Test
    fun reportUuidErrorWithEmptyStrings() {
        val theOnlyTrueUuid = ""
        val backupUuid = ""

        selfReporter.reportUuidError(errorIdentifierPostfix, theOnlyTrueUuid, backupUuid)

        verify(selfReporter).reportError(
            "wrong_uuid_on_startup_init",
            "The only true uuid: ; backup uuid: "
        )
    }
}
