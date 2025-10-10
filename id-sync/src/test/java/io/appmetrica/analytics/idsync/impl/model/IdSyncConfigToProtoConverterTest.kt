package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.concurrent.TimeUnit

internal class IdSyncConfigToProtoConverterTest : CommonTest() {

    private val requestConfig: RequestConfig = mock()
    private val requestConfigProto: IdSyncProtobuf.IdSyncConfig.Request = mock()

    @get:Rule
    val requestConverterRule = constructionRule<RequestConfigToProtoConverter> {
        on { toModel(requestConfigProto) } doReturn requestConfig
        on { fromModel(requestConfig) } doReturn requestConfigProto
    }

    private val converter by setUp { IdSyncConfigToProtoConverter() }

    @Test
    fun `toModel for filled`() {
        val enabledValue = true
        val launchDelayValue = 1000L

        val inputProto = IdSyncProtobuf.IdSyncConfig().apply {
            enabled = enabledValue
            requestConfig = IdSyncProtobuf.IdSyncConfig.RequestConfig().apply {
                requests = listOf(requestConfigProto).toTypedArray()
                launchDelay = launchDelayValue
            }
        }

        ObjectPropertyAssertions(converter.toModel(inputProto))
            .checkField("enabled", enabledValue)
            .checkField("requests", listOf(requestConfig))
            .checkField("launchDelay", launchDelayValue)
            .checkAll()
    }

    @Test
    fun `toModel for empty`() {
        val inputProto = IdSyncProtobuf.IdSyncConfig()
        ObjectPropertyAssertions(converter.toModel(inputProto))
            .checkField("enabled", false)
            .checkField("requests", emptyList<RequestConfig>())
            .checkField("launchDelay", TimeUnit.SECONDS.toMillis(10))
            .checkAll()
    }

    @Test
    fun `fromModel for filled`() {
        val inputConfig = IdSyncConfig(
            enabled = true,
            launchDelay = 1000L,
            requests = listOf(requestConfig)
        )
        ProtoObjectPropertyAssertions(converter.fromModel(inputConfig))
            .checkField("enabled", true)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("requests", arrayOf(requestConfigProto))
                    .checkField("launchDelay", 1000L)
            }
            .checkAll()
    }

    @Test
    fun `fromModel for empty`() {
        val inputConfig = IdSyncConfig(
            enabled = false,
            launchDelay = 0L,
            requests = emptyList()
        )
        ProtoObjectPropertyAssertions(converter.fromModel(inputConfig))
            .checkField("enabled", false)
            .checkFieldRecursively<IdSyncProtobuf.IdSyncConfig.RequestConfig>("requestConfig") { assertions ->
                assertions
                    .checkField("requests", emptyArray<RequestConfig>())
                    .checkField("launchDelay", 0L)
            }
            .checkAll()
    }
}
