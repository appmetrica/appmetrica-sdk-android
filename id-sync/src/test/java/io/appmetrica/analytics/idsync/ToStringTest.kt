package io.appmetrica.analytics.idsync

import io.appmetrica.analytics.idsync.impl.RequestResult
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.testutils.BaseToStringTest
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ToStringTest(
    clazz: Any?,
    actualValue: Any?,
    modifierPreconditions: Int,
    additionalDescription: String?
) : BaseToStringTest(
    clazz,
    actualValue,
    modifierPreconditions,
    additionalDescription
) {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(
                RequestResult::class.java,
                RequestResult(
                    type = "type",
                    isCompleted = true,
                    url = "some url",
                    responseCodeIsValid = true,
                    responseCode = 200,
                    responseBody = ByteArray(10) { it.toByte() },
                    responseHeaders = mapOf("key" to listOf("value#1", "value#2"))
                ),
                0,
                ""
            ),
            arrayOf(
                IdSyncConfig::class.java,
                IdSyncConfig(
                    enabled = true,
                    launchDelay = 100500L,
                    requests = listOf(mock<RequestConfig>(), mock<RequestConfig>())
                ),
                0,
                ""
            ),
            arrayOf(
                Preconditions::class.java,
                Preconditions(networkType = NetworkType.ANY),
                0,
                ""
            ),
            arrayOf(
                RequestConfig::class.java,
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
                ),
                0,
                ""
            )
        )
    }
}
