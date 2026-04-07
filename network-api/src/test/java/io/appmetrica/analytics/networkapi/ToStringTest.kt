package io.appmetrica.analytics.networkapi

import io.appmetrica.analytics.testutils.BaseToStringTest
import io.appmetrica.analytics.testutils.BaseToStringTest.Companion.toTestCase
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

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
    additionalDescription
) {

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            NetworkCallMetrics.Builder()
                .withDnsLookup(10L)
                .withTcpConnect(20L)
                .withTlsHandshake(15L)
                .withTimeToFirstByte(30L)
                .withResponse(40L)
                .withConnectionReused(true)
                .withProtocol("h2")
                .build()
                .toTestCase(),
            NetworkClientSettings.Builder()
                .withConnectTimeout(5000)
                .withReadTimeout(10000)
                .withMaxResponseSize(1024 * 1024)
                .withCollectMetrics(true)
                .build()
                .toTestCase(),
            Request.Builder("https://example.com")
                .withMethod(Request.Method.POST)
                .addHeader("Content-Type", "application/json")
                .build()
                .toTestCase(excludedFields = setOf("body")),
            Response.Builder(isCompleted = true, code = 200, responseData = "test".toByteArray())
                .withUrl("https://example.com")
                .build()
                .toTestCase(excludedFields = setOf("responseData")),
        )
    }
}
