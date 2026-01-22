package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class IdSyncConfigParserTest : CommonTest() {

    private val featureKey = "id_sync"
    private val featuresKey = "features"
    private val featuresListKey = "list"
    private val featureEnabledKey = "enabled"
    private val featureValue = true
    private val blockKey = "id_sync"
    private val launchDelaySecondsKey = "launch_delay_seconds"
    private val requestsKey = "requests"
    private val typeKey = "type"
    private val urlKey = "url"
    private val headersKey = "headers"
    private val resendIntervalValidKey = "resend_interval_for_valid_response"
    private val resendIntervalInvalidKey = "resend_interval_for_invalid_response"
    private val validResponseCodesKey = "valid_response_codes"
    private val preconditionsKey = "preconditions"
    private val networkKey = "network"

    private val requestTypeGet = "GET"
    private val requestUrl = "https://example.com/sync"
    private val authHeaderKey = "Authorization"
    private val authHeaderValue = "Bearer token"
    private val resendIntervalValidValue = 3600L
    private val resendIntervalInvalidValue = 600L
    private val validResponseCode1 = 200
    private val validResponseCode2 = 201
    private val networkCellValue = "cell"
    private val launchDelayValue = 120L
    private val reportEventEnabledKey = "reportEventEnabled"
    private val reportUrlKey = "reportUrl"
    private val reportUrlValue = "https://example.com/report"

    private val converter: IdSyncConfigToProtoConverter = mock()
    private val idSyncConfig: IdSyncConfig = mock()
    private val protoCaptor = argumentCaptor<IdSyncProtobuf.IdSyncConfig>()
    private val parser by setUp { IdSyncConfigParser(converter) }

    @Before
    fun setUp() {
        whenever(converter.toModel(protoCaptor.capture())).thenReturn(idSyncConfig)
    }

    @Test
    fun `parse full config`() {
        // Build JSON programmatically using class properties
        val requestJson = JSONObject().apply {
            put(typeKey, requestTypeGet)
            put(urlKey, requestUrl)
            put(
                headersKey,
                JSONObject().apply {
                    put(
                        authHeaderKey,
                        JSONArray().apply {
                            put(authHeaderValue)
                        }
                    )
                }
            )
            put(resendIntervalValidKey, resendIntervalValidValue)
            put(resendIntervalInvalidKey, resendIntervalInvalidValue)
            put(
                validResponseCodesKey,
                JSONArray().apply {
                    put(validResponseCode1)
                    put(validResponseCode2)
                }
            )
            put(
                preconditionsKey,
                JSONObject().apply {
                    put(networkKey, networkCellValue)
                }
            )
        }

        val requestsArray = JSONArray().apply {
            put(requestJson)
        }

        val idSyncBlockJson = JSONObject().apply {
            put(launchDelaySecondsKey, launchDelayValue)
            put(requestsKey, requestsArray)
            put(reportEventEnabledKey, false)
            put(reportUrlKey, reportUrlValue)
        }

        val featuresJson = JSONObject().apply {
            put(
                featuresListKey,
                JSONObject().apply {
                    put(
                        featureKey,
                        JSONObject().apply {
                            put(featureEnabledKey, featureValue)
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, idSyncBlockJson)
            put(featuresKey, featuresJson)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", featureValue)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(launchDelayValue))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                type = requestTypeGet.toByteArray(Charsets.UTF_8)
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions().apply {
                                    networkType = IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL
                                }
                                url = requestUrl.toByteArray(Charsets.UTF_8)
                                headers = Array(1) {
                                    IdSyncProtobuf.IdSyncConfig.Header()
                                        .apply {
                                            name = authHeaderKey.toByteArray(Charsets.UTF_8)
                                            value = Array(1) { authHeaderValue.toByteArray(Charsets.UTF_8) }
                                        }
                                }
                                resendIntervalForValidResponse = TimeUnit.SECONDS.toMillis(resendIntervalValidValue)
                                resendIntervalForInvalidResponse = TimeUnit.SECONDS.toMillis(resendIntervalInvalidValue)
                                validResponseCodes = intArrayOf(validResponseCode1, validResponseCode2)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse empty config`() {
        val result = parser.parse(JSONObject())
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        emptyArray<IdSyncProtobuf.IdSyncConfig.Request>()
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with empty requests`() {
        val result = parser.parse(
            JSONObject().apply {
                put(requestsKey, JSONArray())
            }
        )
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        emptyArray<IdSyncProtobuf.IdSyncConfig.Request>()
                    )
            }
            .checkAll()
    }

    @Test
    fun `parseConfig with wrong request`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put("Wrong request")
                }
            )
        }
        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(IdSyncProtobuf.IdSyncConfig.Request())
                    )
            }
            .checkAll()
    }

    @Test
    fun `parseConfig for request without headers`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject()
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                validResponseCodes = intArrayOf(200)
                                headers = emptyArray()
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config without cell precondition`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(preconditionsKey, JSONObject())
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with wrong header values`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(
                                headersKey,
                                JSONObject().apply {
                                    put("Some header", "Wrong value")
                                }
                            )
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                headers = arrayOf(
                                    IdSyncProtobuf.IdSyncConfig.Header().apply {
                                        name = "Some header".toByteArray(Charsets.UTF_8)
                                    }
                                )
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with empty response codes`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(validResponseCodesKey, JSONArray())
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with wrong response codes`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(
                                validResponseCodesKey,
                                "Wrong response codes"
                            )
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with wrong response code`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(
                                validResponseCodesKey,
                                JSONArray().apply {
                                    put("Wrong response code")
                                }
                            )
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
            }
            .checkAll()
    }

    @Test
    fun `parse config with zero response code`() {
        val block = JSONObject().apply {
            put(
                requestsKey,
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(
                                validResponseCodesKey,
                                JSONArray().apply {
                                    put(200)
                                }
                            )
                        }
                    )
                }
            )
        }

        val fullJson = JSONObject().apply {
            put(blockKey, block)
        }

        val result = parser.parse(fullJson)
        assertThat(result).isEqualTo(idSyncConfig)

        ProtoObjectPropertyAssertions(protoCaptor.firstValue)
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField(
                        "requests",
                        arrayOf(
                            IdSyncProtobuf.IdSyncConfig.Request().apply {
                                preconditions = IdSyncProtobuf.IdSyncConfig.Preconditions()
                                validResponseCodes = intArrayOf(200)
                            }
                        )
                    )
                    .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
            }
            .checkAll()
    }
}
