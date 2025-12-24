package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

internal class RequestConfigToProtoConverterTest : CommonTest() {

    private val converter by setUp { RequestConfigToProtoConverter() }

    @Test
    fun `toModel for filled`() {
        val typeValue = "type"
        val urlValue = "https://example.com"
        val headerKey = "headerKey"
        val headerValue = "headerValue"
        val headerValues = mapOf(headerKey to listOf(headerValue))
        val resendIntervalForValidResponseValue = 100500L
        val resendIntervalForInvalidResponseValue = 200500L
        val validResponseCodesValue = intArrayOf(200, 204)

        val inputProto = IdSyncProtobuf.IdSyncConfig.Request().apply {
            type = typeValue.toByteArray()
            url = urlValue.toByteArray()
            preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions().apply {
                networkType = IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL
            }
            headers = Array(1) {
                IdSyncProtobuf.IdSyncConfig.Header().apply {
                    name = headerKey.toByteArray()
                    value = arrayOf(headerValue.toByteArray())
                }
            }
            resendIntervalForValidResponse = resendIntervalForValidResponseValue
            resendIntervalForInvalidResponse = resendIntervalForInvalidResponseValue
            validResponseCodes = validResponseCodesValue
            reportEventEnabled = false
            reportUrl = "https://example.com/report"
        }
        ObjectPropertyAssertions(converter.toModel(inputProto))
            .checkField("type", typeValue)
            .checkField("url", urlValue)
            .checkField(
                "preconditions",
                Preconditions(
                    networkType = NetworkType.CELL
                )
            )
            .checkField("headers", headerValues)
            .checkField("resendIntervalForValidResponse", resendIntervalForValidResponseValue)
            .checkField("resendIntervalForInvalidResponse", resendIntervalForInvalidResponseValue)
            .checkField("validResponseCodes", validResponseCodesValue.toList())
            .checkField("reportEventEnabled", false)
            .checkField("reportUrl", "https://example.com/report")
            .checkAll()
    }

    @Test
    fun `toModel for empty`() {
        val inputProto = IdSyncProtobuf.IdSyncConfig.Request()

        ObjectPropertyAssertions(converter.toModel(inputProto))
            .checkField("type", "")
            .checkField("url", "")
            .checkField(
                "preconditions",
                Preconditions(
                    networkType = NetworkType.ANY
                )
            )
            .checkField("headers", emptyMap<String, List<String>>())
            .checkField("resendIntervalForValidResponse", inputProto.resendIntervalForValidResponse)
            .checkField("resendIntervalForInvalidResponse", inputProto.resendIntervalForInvalidResponse)
            .checkField("validResponseCodes", emptyList<Int>())
            .checkField("reportEventEnabled", true)
            .checkField<String?>("reportUrl", null)
            .checkAll()
    }

    @Test
    fun `fromModel for filled`() {
        val typeValue = "type"
        val urlValue = "https://example.com"
        val headerKey = "headerKey"
        val headerValue = "headerValue"
        val headerValues = mapOf(headerKey to listOf(headerValue))
        val resendIntervalForValidResponseValue = 100500L
        val resendIntervalForInvalidResponseValue = 200500L
        val validResponseCodesValue = intArrayOf(200, 204)

        val inputConfig = RequestConfig(
            type = typeValue,
            url = urlValue,
            preconditions = Preconditions(
                networkType = NetworkType.CELL
            ),
            headers = headerValues,
            resendIntervalForValidResponse = resendIntervalForValidResponseValue,
            resendIntervalForInvalidResponse = resendIntervalForInvalidResponseValue,
            validResponseCodes = validResponseCodesValue.toList(),
            reportEventEnabled = false,
            reportUrl = "https://example.com/report"
        )
        val outputProto = converter.fromModel(inputConfig)

        ProtoObjectPropertyAssertions(outputProto)
            .checkField("type", typeValue.toByteArray())
            .checkField("url", urlValue.toByteArray())
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.Preconditions>("preconditions") {
                it.checkField("networkType", IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL)
            }
            .checkField(
                "headers",
                arrayOf(
                    IdSyncProtobuf.IdSyncConfig.Header().apply {
                        name = headerKey.toByteArray()
                        value = arrayOf(headerValue.toByteArray())
                    }
                )
            )
            .checkField("resendIntervalForValidResponse", resendIntervalForValidResponseValue)
            .checkField("resendIntervalForInvalidResponse", resendIntervalForInvalidResponseValue)
            .checkField("validResponseCodes", validResponseCodesValue)
            .checkField("reportEventEnabled", false)
            .checkField("reportUrl", "https://example.com/report")
            .checkAll()
    }

    @Test
    fun `fromModel for empty`() {
        val inputConfig = RequestConfig(
            type = "",
            url = "",
            preconditions = Preconditions(
                networkType = NetworkType.ANY
            ),
            headers = emptyMap(),
            resendIntervalForValidResponse = 0L,
            resendIntervalForInvalidResponse = 0L,
            validResponseCodes = emptyList<Int>(),
            reportEventEnabled = true,
            reportUrl = null
        )

        val outputProto = converter.fromModel(inputConfig)

        ProtoObjectPropertyAssertions(outputProto)
            .checkField("type", "".toByteArray())
            .checkField("url", "".toByteArray())
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.Preconditions>("preconditions") {
                it.checkField("networkType", IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_ANY)
            }
            .checkField(
                "headers",
                emptyArray<IdSyncProtobuf.IdSyncConfig.Header>()
            )
            .checkField("resendIntervalForValidResponse", inputConfig.resendIntervalForValidResponse)
            .checkField("resendIntervalForInvalidResponse", inputConfig.resendIntervalForInvalidResponse)
            .checkField("validResponseCodes", intArrayOf())
            .checkField("reportEventEnabled", true)
            .checkField("reportUrl", "")
            .checkAll()
    }
}
