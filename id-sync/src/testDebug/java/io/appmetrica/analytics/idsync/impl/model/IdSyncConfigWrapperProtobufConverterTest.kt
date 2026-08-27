package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.idsync.internal.IdSyncConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class IdSyncConfigWrapperProtobufConverterTest : CommonTest() {

    private val config: IdSyncConfig = mock()
    private val wrapper = IdSyncConfigWrapper(config)
    private val bytes = ByteArray(3) { it.toByte() }

    private val converter: IdSyncConfigToProtoBytesConverter = mock {
        on { fromModel(config) } doReturn bytes
        on { toModel(bytes) } doReturn config
    }

    private val wrapperConverter = IdSyncConfigWrapperProtobufConverter(converter)

    @Test
    fun fromModelDelegatesToInnerConverter() {
        assertThat(wrapperConverter.fromModel(wrapper)).isSameAs(bytes)
    }

    @Test
    fun toModelDelegatesAndWraps() {
        assertThat(wrapperConverter.toModel(bytes).config).isSameAs(config)
    }
}
