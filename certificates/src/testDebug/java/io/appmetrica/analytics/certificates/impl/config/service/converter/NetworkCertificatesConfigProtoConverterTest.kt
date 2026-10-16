package io.appmetrica.analytics.certificates.impl.config.service.converter

import io.appmetrica.analytics.certificates.impl.RemoteNetworkCertificatesConfigProto
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test

internal class NetworkCertificatesConfigProtoConverterTest : CommonTest() {

    private val converter by setUp { NetworkCertificatesConfigProtoConverter() }

    @Test
    fun fromModel() {
        val model = ServiceSideNetworkCertificatesConfig(enabled = false)
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("enabled", false)
            .checkAll()
    }

    @Test
    fun toModel() {
        val remote = RemoteNetworkCertificatesConfigProto().also { it.enabled = false }
        val model = converter.toModel(remote)
        ObjectPropertyAssertions(model)
            .checkField("enabled", false)
            .checkAll()
    }
}
