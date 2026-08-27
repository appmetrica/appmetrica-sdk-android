package io.appmetrica.analytics.adrevenue.other.impl.config.service

import io.appmetrica.analytics.adrevenue.other.impl.AdRevenueOtherConfigProto
import io.appmetrica.analytics.adrevenue.other.impl.config.service.converter.AdRevenueOtherConfigProtoConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideAdRevenueOtherConfigConverterTest : CommonTest() {

    private val config: ServiceSideAdRevenueOtherConfig = mock()
    private val proto: AdRevenueOtherConfigProto = mock()
    private val byteArray = "byteArray".toByteArray()

    private val protoConverter: AdRevenueOtherConfigProtoConverter = mock {
        on { fromModel(config) } doReturn proto
        on { toModel(proto) } doReturn config
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(proto) } doReturn byteArray
    }

    @get:Rule
    val adRevenueOtherConfigProtoRule = staticRule<AdRevenueOtherConfigProto> {
        on { AdRevenueOtherConfigProto.parseFrom(byteArray) } doReturn proto
    }

    private val converter = ServiceSideAdRevenueOtherConfigConverter(
        protoConverter = protoConverter
    )

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(config)).isSameAs(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isSameAs(config)
    }
}
