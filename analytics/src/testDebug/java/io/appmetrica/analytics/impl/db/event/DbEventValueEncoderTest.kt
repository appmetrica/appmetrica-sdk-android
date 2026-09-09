package io.appmetrica.analytics.impl.db.event

import android.util.Base64
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
internal class DbEventValueEncoderTest : CommonTest() {

    private val payload = "payload".toByteArray(StandardCharsets.UTF_8)

    @Test
    fun encodeNull() {
        assertThat(DbEventValueEncoder.encode(null, InternalEvents.EVENT_TYPE_REGULAR.typeId, null))
            .isNull()
    }

    @Test
    fun encodeTextEventAsUtf8String() {
        assertThat(
            DbEventValueEncoder.encode(payload, InternalEvents.EVENT_TYPE_REGULAR.typeId, null)
        ).isEqualTo("payload")
    }

    @Test
    fun encodeBinaryEventAsBase64() {
        val expected = String(Base64.encode(payload, Base64.DEFAULT), StandardCharsets.UTF_8)
        assertThat(
            DbEventValueEncoder.encode(payload, InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId, null)
        ).isEqualTo(expected)
    }

    @Test
    fun encodeCustomEventProtocolBelow2AsUtf8() {
        assertThat(
            DbEventValueEncoder.encode(payload, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId, 1)
        ).isEqualTo("payload")
        assertThat(
            DbEventValueEncoder.encode(payload, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId, null)
        ).isEqualTo("payload")
    }

    @Test
    fun encodeCustomEventProtocolAtLeast2AsBase64() {
        val expected = String(Base64.encode(payload, Base64.DEFAULT), StandardCharsets.UTF_8)
        assertThat(
            DbEventValueEncoder.encode(payload, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId, 2)
        ).isEqualTo(expected)
    }

    @Test
    fun encodeNativeCrashAsUtf8Base64Text() {
        val base64Text = String(Base64.encode(payload, Base64.DEFAULT), StandardCharsets.UTF_8)
        assertThat(
            DbEventValueEncoder.encode(
                base64Text.toByteArray(StandardCharsets.UTF_8),
                InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF.typeId,
                null
            )
        ).isEqualTo(base64Text)
    }
}
