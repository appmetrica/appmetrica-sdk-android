package io.appmetrica.analytics

import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Unit test example for discovering client-server proto parsing issues. It is the "Hello world" for
 * fast start of investigation.
 * Just replace test/resources/proto.bin with remote proto message and check it.
 * Usually back-end can provide proto.bin file with some proto message.
 * CrashAndroid.Anr is just an example.
 */
class RemoteProtoTest : CommonTest() {

    @Test
    fun parseFrom() {
        val stream = javaClass.classLoader.getResourceAsStream("proto.bin")
        val data = stream.readBytes()
        val message = CrashAndroid.Anr.parseFrom(data)
        assertThat(message.buildId).isNotEmpty
    }
}
