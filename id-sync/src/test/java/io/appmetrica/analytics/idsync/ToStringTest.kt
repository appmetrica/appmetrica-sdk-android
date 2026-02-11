package io.appmetrica.analytics.idsync

import io.appmetrica.analytics.idsync.impl.RequestResult
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.testutils.BaseToStringTest
import io.appmetrica.analytics.testutils.BaseToStringTest.Companion.toTestCase
import io.appmetrica.analytics.testutils.ToStringTestUtils
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
internal class ToStringTest(
    actualValue: Any?,
    modifierPreconditions: Int,
    excludedFields: Set<String>?,
    additionalDescription: String?
) : BaseToStringTest(
    actualValue,
    modifierPreconditions,
    excludedFields,
    additionalDescription,
    // RequestResult uses native array format, so support all three formats
    setOf(
        ToStringTestUtils.ArrayFormat.FULL,
        ToStringTestUtils.ArrayFormat.SHORT,
        ToStringTestUtils.ArrayFormat.NATIVE
    )
) {

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            RequestResult(
                type = "type",
                isCompleted = true,
                url = "some url",
                responseCodeIsValid = true,
                responseCode = 200,
                responseBody = ByteArray(10) { it.toByte() },
                responseHeaders = mapOf("key" to listOf("value#1", "value#2"))
            ).toTestCase(),
            IdSyncConfig(
                enabled = true,
                launchDelay = 100500L,
                requests = listOf(mock<RequestConfig>(), mock<RequestConfig>())
            ).toTestCase(),
            Preconditions(networkType = NetworkType.ANY).toTestCase(),
            RequestConfig(
                type = "type",
                url = "Some url",
                preconditions = mock<Preconditions>(),
                headers = mapOf("key" to listOf("value#", "value#2")),
                resendIntervalForValidResponse = 100500L,
                resendIntervalForInvalidResponse = 200500L,
                validResponseCodes = listOf(200, 204),
                reportEventEnabled = true,
                reportUrl = null
            ).toTestCase()
        )
    }
}
